package info.kgeorgiy.ja.belickij.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static info.kgeorgiy.ja.belickij.walk.RecursiveWalk.write;

public class TreeVisitor extends SimpleFileVisitor<Path> {

    final private BufferedWriter to;

    TreeVisitor(BufferedWriter to) {
        this.to = to;
    }

    private long PJW(byte[] buff, int count, long h) {
        long high;
        for (int i = 0; i < Math.min(buff.length, count); i++) {
            h = (h << 8) + (buff[i] & 0xFF);
            if ((high = (h & 0xFF00000000000000L)) != 0) {
                h = h ^ (high >> 48);
            }
            h = h & ~high;
        }
        return h;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) {
        try (InputStream br = Files.newInputStream(path)) {
            byte[] bytes = new byte[1024];
            long h = 0L;
            int c;
            while ((c = br.read(bytes)) >= 0) {
                h = PJW(bytes, c, h);
            }
            write(h, path.toString(), to);
        } catch (IOException e) {
            System.err.println("Failed to read from file: " + path);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException exc) {
        System.err.println("Failed to visit file: " + path);
        write(0, path.toString(), to);
        return FileVisitResult.CONTINUE;
    }

}
