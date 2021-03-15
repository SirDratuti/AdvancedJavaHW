package info.kgeorgiy.ja.belickij.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class RecursiveWalk {

    public static void main(String[] args) {

        final Path from;
        final Path to;

        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Incorrect program input");
            return;
        }

        try {
            from = Paths.get(args[0]).normalize();
        } catch (InvalidPathException e) {
            System.err.println("Input file's path is incorrect");
            return;
        }

        try {
            to = Paths.get(args[1]);
            if (Files.notExists(to.getParent())) {
                Files.createDirectories(to.getParent());
            }
        } catch (InvalidPathException e) {
            System.err.println("Output file's path is incorrect");
            return;
        } catch (IOException e) {
            System.err.println("Failed to create directory for output file");
            return;
        } catch (NullPointerException e) {
            System.err.println("Output directory is null");
            return;
        }

        try (BufferedReader br = Files.newBufferedReader(from, StandardCharsets.UTF_8)) {
            try (BufferedWriter bw = Files.newBufferedWriter(to, StandardCharsets.UTF_8)) {
                try {
                    String currentFile;
                    while ((currentFile = br.readLine()) != null) {
                        parse(currentFile, bw);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to read from input file: " + from);
                }
            } catch (IOException e) {
                System.err.println("Failed to create output writer: " + to);
            }
        } catch (IOException e) {
            System.err.println("Failed to create input reader: " + from);
        }
    }

    private static void parse(String path, BufferedWriter to) {
        try {
            Files.walkFileTree(Paths.get(path), new TreeVisitor(to));
        } catch (IOException e) {
            System.err.println("Failed to walk file tree for: " + path);
        } catch (InvalidPathException e) {
            write(0, path, to);
        }
    }

    static void write(long h, String path, BufferedWriter to) {
        try {
            to.write(String.format("%016x", h) + " " + path);
            to.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write hash for: " + path);
        }
    }
}
