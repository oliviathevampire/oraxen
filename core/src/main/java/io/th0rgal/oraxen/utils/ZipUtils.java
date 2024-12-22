package io.th0rgal.oraxen.utils;

import io.th0rgal.oraxen.config.Settings;
import io.th0rgal.oraxen.pack.generation.DuplicationHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.zip.*;

public class ZipUtils {

    private ZipUtils() {
    }

    public static void writeZipFile(final File outputFile,
                                    final List<VirtualFile> fileList) {

        try (final FileOutputStream fos = new FileOutputStream(outputFile);
             final ZipOutputStream zos = new ZipOutputStream(fos, StandardCharsets.UTF_8)) {
            final int compressionLevel = Deflater.class.getDeclaredField(Settings.COMPRESSION.toString()).getInt(null);
            zos.setLevel(compressionLevel);
            zos.setComment(Settings.COMMENT.toString());
            for (final VirtualFile file : fileList) {
                try (InputStream fis = file.getInputStream()) {
                    if (fis == null) {
                        System.err.println("Skipping file with null InputStream: " + file.getPath());
                        continue; // Skip files with null InputStream
                    }
                    addToZip(file.getPath(), fis, zos);
                } catch (IOException e) {
                    e.printStackTrace(); // Log the error for debugging
                }
            }

        } catch (final IOException | NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    public static void addToZip(String zipFilePath, final InputStream fis, ZipOutputStream zos) throws IOException {
        if (fis == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        final ZipEntry zipEntry = new ZipEntry(zipFilePath);
        zipEntry.setLastModifiedTime(FileTime.fromMillis(0L));
        DuplicationHandler.checkForDuplicate(zos, zipEntry);

        try {
            final byte[] bytes = new byte[1024];
            int length;
            Checksum checksum = new CRC32();
            long totalSize = 0;

            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
                checksum.update(bytes, 0, length);
                totalSize += length;
            }

            if (Settings.PROTECTION.toBool()) {
                zipEntry.setCrc(checksum.getValue());
                zipEntry.setSize(totalSize);
            }
        } catch (IOException e) {
            throw new IOException("Error writing to zip entry", e);
        } finally {
            zos.closeEntry();
        }
    }
}
