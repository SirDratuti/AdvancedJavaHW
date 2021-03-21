package info.kgeorgiy.ja.belickij.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Implementor implements Impler {

    public static void main(String[] args) throws ClassNotFoundException, ImplerException {
        Implementor implementor = new Implementor();
        implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
    }

    private void addMethod(Method method, Writer fileWriter) throws IOException {
        fileWriter.write(Modifier.toString(method.getModifiers() & ~(Modifier.ABSTRACT | Modifier.TRANSIENT))
                + " " + method.getReturnType().getCanonicalName() + " "
                + method.getName() + " ");
        addParameterTypes(method.getParameters(), fileWriter);
        addThrowTypes(method.getExceptionTypes(), fileWriter);
        fileWriter.write("{\n");
        fileWriter.write("\treturn ");
        if (method.getReturnType().equals(boolean.class)) {
            fileWriter.write("false;");
        } else if (method.getReturnType().equals(void.class)) {
            fileWriter.write(";");
        } else if (method.getReturnType().isPrimitive()) {
            fileWriter.write("0;");
        } else {
            fileWriter.write("null;");
        }
        fileWriter.write("\n}\n\n");
    }

    private void addParameterTypes(Parameter[] parameters, Writer fileWriter) throws IOException {
        fileWriter.write("(");
        for (int i = 0; i < parameters.length; i++) {
            fileWriter.write(parameters[i].getType().getCanonicalName() + " "
                    + parameters[i].getName());
            if (i != parameters.length - 1) {
                fileWriter.write(", ");
            }
        }
        fileWriter.write(")");
    }

    private void addThrowTypes(Type[] exceptions, Writer fileWriter) throws IOException {
        if (exceptions.length > 0) {
            fileWriter.write(" throws ");
            for (int i = 0; i < exceptions.length; i++) {
                fileWriter.write(exceptions[i].getTypeName());
                if (i != exceptions.length - 1) {
                    fileWriter.write(",");
                }
            }
        }
    }

    private void addPackage(Writer fileWriter, Class<?> token) throws IOException {
        if (token.getPackage() != null) {
            fileWriter.write("package " + getPackageName(token) + ";\n");
        }
    }

    private void addHeader(Class<?> token, Writer fileWriter) throws IOException {
        fileWriter.write("public class " + token.getSimpleName() + "Impl ");
        fileWriter.write(token.isInterface() ? "implements " : "extends ");
        fileWriter.write(token.getSimpleName());
        fileWriter.write("{\n");
    }

    private void addConstructor(Constructor<?> constructor, Writer fileWriter, Class<?> token) throws IOException {
        if ((constructor.getModifiers() & ~Modifier.PRIVATE) == constructor.getModifiers()) {
            fileWriter.write(Modifier.toString(constructor.getModifiers() & ~(Modifier.ABSTRACT))
                    + " " + token.getSimpleName() + "Impl");
            addParameterTypes(constructor.getParameters(), fileWriter);
            fileWriter.write("{\n");
            fileWriter.write("\tsuper(");
            Parameter[] parameters = constructor.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                fileWriter.write(parameters[i].getName());
                if (i != parameters.length - 1) {
                    fileWriter.write(", ");
                }
            }
            fileWriter.write(");");
            fileWriter.write("\n}\n\n");
        }
    }

    private String getPackageName(Class aClass) {
        if (aClass.getPackage() == null)
            return "";
        return aClass.getPackage().getName();
    }


    private void createDirectory(Path path) throws ImplerException {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            } catch (IOException | SecurityException e) {
                throw new ImplerException("Unable to create directories for output file " + e.getMessage());
            }
        }
    }

    private void generateMethods(Class<?> token, Writer fileWriter) throws IOException {
        Set<Method> methodSet = new TreeSet<>(Comparator.comparingInt(
                (Method method) -> (method.getName() + Arrays.toString(method.getParameterTypes())).hashCode()));
        Class<?> tempToken = token;
        Arrays.stream(token.getMethods())
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .collect(Collectors.toCollection(() -> methodSet));
        while (tempToken != null) {
            Arrays.stream(tempToken.getDeclaredMethods())
                    .filter(method -> Modifier.isAbstract(method.getModifiers()))
                    .collect(Collectors.toCollection(() -> methodSet));
            tempToken = tempToken.getSuperclass();
        }

        for (Method method : methodSet) {
            addMethod(method, fileWriter);
        }

    }

    private void generateConstructors(Class<?> token, Writer fileWriter) throws IOException {
        Constructor<?>[] constructors = token.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            addConstructor(constructor, fileWriter, token);
        }
        fileWriter.write("\n}");
    }

    private Path getPathToCreatedFile(Class aClass, Path path) {
        return path.resolve(aClass.getCanonicalName().replace('.', File.separatorChar) + "Impl.java");
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {

        if (token == void.class || token == Enum.class) {
            throw new ImplerException("I may not implement this");
        }

        root = getPathToCreatedFile(token, root);
        createDirectory(root);

        try (Writer fileWriter = Files.newBufferedWriter(root)) {
            addPackage(fileWriter, token);
            addHeader(token, fileWriter);
            generateMethods(token, fileWriter);
            generateConstructors(token, fileWriter);
        } catch (final IOException e) {
            throw new ImplerException("Error while generating class :" + token.getCanonicalName());
        }
    }
}
