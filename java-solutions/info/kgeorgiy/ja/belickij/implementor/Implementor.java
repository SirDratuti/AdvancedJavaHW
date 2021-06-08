package info.kgeorgiy.ja.belickij.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * @author Andrew Belickij
 */
public class Implementor implements JarImpler {

    /**
     * Starter(main) method of {@link Implementor}
     *
     * @param args user input
     * @throws ImplerException if {@link #implement(Class, Path)} } or {@link #implementJar(Class, Path)} throws {@link ImplerException} or
     *                         {@link Class#forName(String)} throws {@link ClassNotFoundException}.
     */
    public static void main(String[] args) throws ImplerException {
        Implementor implementor = new Implementor();
        try {
            if (args.length == 3 && args[0].equals("-jar")) {
                implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            } else if (args.length == 2) {
                implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
            }
        } catch (final ClassNotFoundException e) {
            throw new ImplerException("Can't find class from input arguments");
        }
    }

    /**
     * Generating given method.
     * Invoke {@link #addMethodHeader(Method, Writer)} to generate head of method,
     * {@link #addParameterTypes(Parameter[], Writer)} to generate method parameters,
     * {@link #addThrowTypes(Type[], Writer)} to generate throwing types and
     * {@link #addDefaultReturn(Method, Writer)} to generate default return realization.
     *
     * @param method     a method to be implemented.
     * @param fileWriter a writer to file.
     * @throws IOException if invoked methods throw {@link java.io.IOException}
     */
    private void addMethod(final Method method, final Writer fileWriter) throws IOException {
        if (!Modifier.isFinal(method.getModifiers())) {
            addMethodHeader(method, fileWriter);
            addParameterTypes(method.getParameters(), fileWriter);
            addThrowTypes(method.getExceptionTypes(), fileWriter);
            addDefaultReturn(method, fileWriter);
        }
    }

    /**
     * Generate modifiers, return type and name to given method
     *
     * @param method     a method to be implemented
     * @param fileWriter a writer to file
     * @throws IOException if error while writing to file
     * @see Method#getModifiers()
     * @see Modifier
     */
    private void addMethodHeader(final Method method, final Writer fileWriter) throws IOException {
        fileWriter.write(Modifier.toString(method.getModifiers() & ~(Modifier.ABSTRACT | Modifier.TRANSIENT))
                + " " + resolveString(method.getReturnType().getCanonicalName())
                + " " + resolveString(method.getName()) + " ");
    }

    /**
     * Generating value to return.
     * It returns false if return type is {@code boolean},
     * returns nothing if return type is {@code void},
     * returns {@code 0} if return type is primitive,
     * else returns {@code null}.
     *
     * @param method     a method to generate return value.
     * @param fileWriter a writer to file.
     * @throws IOException if error with writing to file.
     */
    private void addDefaultReturn(final Method method, final Writer fileWriter) throws IOException {
        fileWriter.write("{" + System.lineSeparator() + "return ");
        if (method.getReturnType().equals(boolean.class)) {
            fileWriter.write("false;");
        } else if (method.getReturnType().equals(void.class)) {
            fileWriter.write(";");
        } else if (method.getReturnType().isPrimitive()) {
            fileWriter.write("0;");
        } else {
            fileWriter.write("null;");
        }
        fileWriter.write(System.lineSeparator() + "}" + System.lineSeparator() + System.lineSeparator());
    }

    /**
     * Generates method's parameters.
     * Write {@link Parameter#getType()} and {@link Parameter#getName()} for each given parameter.
     *
     * @param parameters an array of method's parameters.
     * @param fileWriter a writer to file.
     * @throws IOException if error while writing to file.
     */
    private void addParameterTypes(final Parameter[] parameters, final Writer fileWriter) throws IOException {
        fileWriter.write("(" + Arrays.stream(parameters).map(parameter -> resolveString(parameter.getType().getCanonicalName()
                + " " + parameter.getName()))
                .collect(Collectors.joining(", ")) + ")");
    }

    /**
     * Generate method's throw types.
     * Write {@link Type#getTypeName()} for each given exception type.
     *
     * @param exceptions an array of method's throw types.
     * @param fileWriter a writer to file.
     * @throws IOException if error while writing to file.
     */
    private void addThrowTypes(final Type[] exceptions, final Writer fileWriter) throws IOException {
        if (exceptions.length > 0) {
            fileWriter.write(" throws " + Arrays.stream(exceptions).map(type -> resolveString(type.getTypeName().replace('$', '.')))
                    .collect(Collectors.joining(", ")));
        }
    }

    /**
     * Generate package name for given token
     *
     * @param fileWriter a writer to file.
     * @param token      type token to create implementation for.
     * @throws IOException if error while writing to file.
     */
    private void addPackage(final Writer fileWriter, final Class<?> token) throws IOException {
        if (!token.getPackageName().equals("")) {
            fileWriter.write("package " + token.getPackageName() + ";" + System.lineSeparator());
        }
    }

    /**
     * Generates class header.
     * Write given token's class name + {@code Impl} and write correct {@code implement} or {@code extends}.
     *
     * @param token      type token to create implementation for.
     * @param fileWriter a writer to file
     * @throws IOException if error while writing to file
     * @see #resolveString(String)
     */
    private void addClassHeader(final Class<?> token, final Writer fileWriter) throws IOException {
        fileWriter.write("public class " + resolveString(token.getSimpleName()) + "Impl ");
        fileWriter.write(token.isInterface() ? "implements " : "extends ");
        fileWriter.write(resolveString(token.getCanonicalName()));
    }

    /**
     * Transform symbols from string to unicode.
     *
     * @param str a given string.
     * @return {@link String} with unicode symbols if needed.
     */
    private String resolveString(String str) {
        StringBuilder b = new StringBuilder();

        str.chars()
                .forEach(currentChar -> {
                    if (currentChar >= 128) {
                        b.append("\\u").append(String.format("%04x", currentChar));
                    } else {
                        b.append((char) currentChar);
                    }
                });

        return b.toString();
    }

    /**
     * Generates given constructor.
     *
     * @param constructor a constructor.
     * @param fileWriter  a writer to file.
     * @param token       type token to create implementation for.
     * @throws IOException if error while adding constructor.
     */
    private void addConstructor(final Constructor<?> constructor, final Writer fileWriter, final Class<?> token) throws IOException {
        if (addConstructorHeader(constructor, token, fileWriter)) {
            addThrowTypes(constructor.getExceptionTypes(), fileWriter);
            addConstructorRealization(constructor, fileWriter);
        }
    }

    /**
     * Generates realization of given constructor.
     *
     * @param constructor a given constructor to add realization for.
     * @param fileWriter  a writer to file.
     * @throws IOException if error while generating constructor realization.
     */
    private void addConstructorRealization(final Constructor<?> constructor, final Writer fileWriter) throws IOException {
        fileWriter.write("{" + System.lineSeparator() + "super(" +
                Arrays.stream(constructor.getParameters())
                        .map(param -> resolveString(param.getName()))
                        .collect(Collectors.joining(", ")) + ");" + System.lineSeparator() + "}" + System.lineSeparator() + System.lineSeparator());
    }

    /**
     * Generates given constructor.
     *
     * @param constructor a given constructor to add.
     * @param token       type token to create implementation for.
     * @param fileWriter  a writer to file.
     * @return {@link Boolean#FALSE} if given constructor is {@code private} or {@code transient}.
     * @throws IOException if error while adding  constructor.
     */
    private boolean addConstructorHeader(final Constructor<?> constructor, final Class<?> token, final Writer fileWriter) throws IOException {
        if ((constructor.getModifiers() & ~Modifier.PRIVATE & ~Modifier.TRANSIENT) == constructor.getModifiers()) {
            fileWriter.write(Modifier.toString(constructor.getModifiers() & ~(Modifier.ABSTRACT))
                    + " " + resolveString(token.getSimpleName()) + "Impl");
            addParameterTypes(constructor.getParameters(), fileWriter);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates directory by given path.
     *
     * @param path is directory which we want to create.
     * @throws ImplerException if troubles while creating directory.
     */
    private void createDirectory(final Path path) throws ImplerException {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException | SecurityException e) {
                throw new ImplerException("Error while creating file directory " + e.getMessage());
            }
        }
    }

    /**
     * Adds all given methods to given set.
     *
     * @param set     a set to add methods.
     * @param methods an array of methods to add.
     */
    private void addToSet(final Set<Method> set, final Method[] methods) {
        Arrays.stream(methods)
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .forEach(set::add);
    }

    /**
     * Generates given methods.
     * Invoke {@link #addMethod} every given method.
     *
     * @param methodSet  a set of methods with different signatures.
     * @param fileWriter a writer to file.
     * @throws IOException if error while adding methods.
     */
    private void addGeneratedMethods(final Set<Method> methodSet, final Writer fileWriter) throws IOException {
        for (Method method : methodSet) {
            addMethod(method, fileWriter);
        }
    }

    /**
     * Generates method for class to implement.
     * Get all methods using {@link Class#getMethods()} and all declared methods from superclasses.
     * Use {@link TreeSet} to group methods with similar signature.
     *
     * @param token      type token to create implementation for.
     * @param fileWriter a writer to file.
     * @throws IOException if error while generating methods.
     * @see Class#getDeclaredMethods()
     * @see Class#getSuperclass()
     */
    private void generateMethods(Class<?> token, final Writer fileWriter) throws IOException {
        Set<Method> methodSet = new TreeSet<>(Comparator.comparingInt(
                (Method method) -> (method.getName().hashCode() + Arrays.hashCode(method.getParameterTypes()))));

        addToSet(methodSet, token.getMethods());

        while (token != null) {
            addToSet(methodSet, token.getDeclaredMethods());
            token = token.getSuperclass();
        }

        addGeneratedMethods(methodSet, fileWriter);
    }

    /**
     * Generates constructors for class to implement.
     * Invoke {@link #addConstructor(Constructor, Writer, Class)} for every declared constructor.
     *
     * @param token      type token to create implementation for.
     * @param fileWriter a writer to file.
     * @throws IOException if error while adding constructors.
     * @see Class#getDeclaredConstructors()
     */
    private void generateConstructors(final Class<?> token, final Writer fileWriter) throws IOException {
        for (Constructor<?> constructor : token.getDeclaredConstructors()) {
            addConstructor(constructor, fileWriter, token);
        }
    }

    /**
     * Generates name for class to generate.
     *
     * @param token type token to create implementation for.
     * @return name of token's class + {@code Impl}
     */
    private String getImplClassName(final Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    /**
     * Checks if input is correct.
     *
     * @param token type token to create implementation for.
     * @param root  a path where to create implementation.
     * @throws ImplerException if given input is incorrect.
     */
    private void checkInput(final Class<?> token, final Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Incorrect null input");
        }
        if (token.isPrimitive() || token == Enum.class || token.isArray()) {
            throw new ImplerException("Can't implement this " + token.getCanonicalName());
        }
        if (Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Can't extend final class");
        }
        if (token.isMemberClass() && Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Can't implement this because of private modifier" + token.getCanonicalName());
        }
        if (token.getPackageName().startsWith("javax")) {
            throw new ImplerException("Can't extend javax.*");
        }
    }

    /**
     * Generates class and writes it using {@code Writer}
     * Invoke {@link #generateMethods(Class, Writer)} to generate methods
     * Invoke {@link #generateConstructors(Class, Writer)} to generate constructors
     *
     * @param token      type token to create implementation for.
     * @param fileWriter writer to file
     * @throws IOException if inner methods
     */
    private void addClass(final Class<?> token, final Writer fileWriter) throws IOException {
        if (!Modifier.isPrivate(token.getModifiers())) {
            addPackage(fileWriter, token);
            addClassHeader(token, fileWriter);
            addClassBody(token, fileWriter);
        }
    }

    /**
     * Generates overridden methods and constructors.
     * Invoke {@link #generateMethods(Class, Writer)} to generate methods
     * Invoke {@link #generateConstructors(Class, Writer)} to generate constructors
     *
     * @param token      type token to create implementation for.
     * @param fileWriter writer to generated class's file
     * @throws IOException if invoked methods throw {@link IOException}
     */
    private void addClassBody(final Class<?> token, final Writer fileWriter) throws IOException {
        fileWriter.write("{" + System.lineSeparator());
        generateMethods(token, fileWriter);
        generateConstructors(token, fileWriter);
        fileWriter.write(System.lineSeparator() + "}");
    }

    /**
     * Generates class with implementation of given one.
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws ImplerException if input is incorrect and if error with creating directory or output writer.
     * @see info.kgeorgiy.java.advanced.implementor.Impler
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        checkInput(token, root);
        final Path path = root.resolve(Paths.get(token.getPackageName().replace('.', File.separatorChar),
                getImplClassName(token) + ".java"));
        createDirectory(path);
        try (Writer fileWriter = Files.newBufferedWriter(path)) {
            addClass(token, fileWriter);
        } catch (final IOException e) {
            throw new ImplerException("Error while generating class " + token.getCanonicalName());
        }
    }


    /**
     * Implements jar file by using given class token.
     * Use {@link #implement(Class, Path)} method for implementing <var> .java </var>class
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException if error while implementing file or jar
     * @see #implement(Class, Path)
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        checkInput(token, jarFile);
        createDirectory(jarFile);
        final Path jarDirectory = jarFile.toAbsolutePath().getParent();
        implement(token, jarDirectory);
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile), generateManifest())) {
            JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
            if (javaCompiler.run(
                    null,
                    null,
                    null,
                    generateArguments(token, jarDirectory)) != 0) {
                throw new ImplerException("Error while compiling implemented class");
            } else {
                writeToJar(jarOutputStream, token, jarDirectory);
            }
        } catch (final IOException e) {
            throw new ImplerException("Can't create stream to jar " + e.getMessage());
        }

    }

    /**
     * Writes {@link ZipEntry} to {@code tempDirectory}
     *
     * @param jarOutputStream output stream for jar-file
     * @param token           type token to create implementation for.
     * @param tempDirectory   a temp directory for implemented .java class
     * @throws IOException if problem with writing to {@link JarOutputStream}
     */
    private void writeToJar(final JarOutputStream jarOutputStream, final Class<?> token, final Path tempDirectory) throws IOException {
        jarOutputStream.putNextEntry(new ZipEntry(pathToJarPath(token)));
        Files.copy(getFile(token, tempDirectory, ".class"), jarOutputStream);
    }

    /**
     * Generates path for {@code ZipEntry}.
     *
     * @param token type token to create implementation for.
     * @return {@code String} path to file with {@code /} separator
     */
    private String pathToJarPath(final Class<?> token) {
        return Paths.get(token.getName()
                .replace('.', '/') + "Impl.class")
                .getParent()
                .resolve(token.getSimpleName() + "Impl.class")
                .toString()
                .replace('\\', '/');
    }


    /**
     * Generating full class name by token
     *
     * @param token type token to create implementation for.
     * @return package name with {@link #getImplClassName(Class)}
     */
    private String getImplName(final Class<?> token) {
        return token.getPackageName() + "." + getImplClassName(token);
    }


    /**
     * Generating manifest for jar-file. Using 1.0 for {@link Attributes.Name#MANIFEST_VERSION}
     * and author name for {@link Attributes.Name#IMPLEMENTATION_VENDOR}
     *
     * @return generated manifest for jar-file
     */
    private Manifest generateManifest() {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        return manifest;
    }

    /**
     * Generates last argument for {@link JavaCompiler#run(InputStream, OutputStream, OutputStream, String...)}.
     * Generates classpath using {@link #getFile(Class, Path, String)} and filepath using{@link Path#of(URI)}
     *
     * @param token type token to create implementation for.
     * @param path  directory of generated .java class
     * @return {@code String[]} with generated arguments
     * @throws ImplerException if unable to get classpath
     */
    private String[] generateArguments(final Class<?> token, final Path path) throws ImplerException {
        final String classPath;
        try {
            classPath = Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new ImplerException("Unable to compile code: URL cannot be converted to URI.", e);
        }
        final String filePath = getFile(token, path, ".java").toString();

        return new String[]{"-cp", classPath, "-encoding", "UTF-8", filePath};
    }


    /**
     * Generates full path for file.
     * Resolve Impl class name with correct separator and suffix
     *
     * @param token  type token to create implementation for.
     * @param path   a current path from {@link #generateArguments(Class, Path)}
     * @param suffix a class suffix
     * @return {@code Path}
     */
    private Path getFile(final Class<?> token, final Path path, final String suffix) {
        return path.resolve(getImplName(token)
                .replace(".", File.separator) + suffix)
                .toAbsolutePath();
    }

}
