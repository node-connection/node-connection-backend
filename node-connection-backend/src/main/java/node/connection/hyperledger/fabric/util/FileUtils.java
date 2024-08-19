package node.connection.hyperledger.fabric.util;

import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    public static void write(String filePath, String v) {
        write(Paths.get(filePath).toFile(), v);
    }

    public static void write(File destination, String v) {
        try (FileOutputStream out = new FileOutputStream(destination)) {
            out.write(v.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FILE_IO_EXCEPTION);
        }
    }

    public static String read(String filePath) {
        return read(Paths.get(filePath));
    }

    public static String read(Path path) {
        try {
            byte[] b = Files.readAllBytes(path);
            return new String(b);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FILE_IO_EXCEPTION);
        }
    }

    public static boolean exists(String filePath) {
        return exists(Paths.get(filePath));
    }

    public static boolean exists(Path path) {
        return Files.exists(path);
    }
}
