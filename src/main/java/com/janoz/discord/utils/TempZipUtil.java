package com.janoz.discord.utils;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@UtilityClass
public class TempZipUtil {

    /**
     * Extracts the entries of a ZIP file from the provided {@link ZipInputStream} into a temporary directory.
     * The temporary directory is created with a specified prefix and is automatically marked for deletion on JVM exit.
     * Throws an {@link IOException} if file operations fail or if a potential Zip Slip vulnerability is detected.
     *
     * @param zipInputStream the {@link ZipInputStream} containing ZIP entries to be extracted
     * @param dirPrefix the prefix to use for naming the temporary directory
     * @return the temporary directory containing the extracted entries
     * @throws IOException if an I/O error occurs during extraction or a security violation is detected
     */
    public static File tempUnzip(ZipInputStream zipInputStream, String dirPrefix) throws IOException {
        File tempDir = Files.createTempDirectory(dirPrefix).toFile();
        tempDir.deleteOnExit();
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (zipEntry.isDirectory()) {
                continue;
            }
            File file = constructFile(tempDir, zipEntry.getName());
            Files.copy(zipInputStream, file.toPath());
            zipInputStream.closeEntry();
        }
        return tempDir;
    }


    private static File constructFile(File root, String file) throws IOException {
        File result = new File(root,file);
        if (!result.getCanonicalPath().startsWith(root.getCanonicalPath())) {
            throw new IOException("Zip Slip vulnerability detected for " + file);
        }
        createDir(result.getParentFile());
        result.deleteOnExit();
        return result;
    }

    private static void createDir(File dir) throws IOException {
        if (dir.exists()) {
            return;
        }
        createDir(dir.getParentFile());
        if (!dir.mkdir()) {
            throw new IOException("Unable to create directory " + dir);
        }
        dir.deleteOnExit();
    }
}
