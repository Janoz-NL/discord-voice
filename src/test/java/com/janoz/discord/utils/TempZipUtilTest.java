package com.janoz.discord.utils;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class TempZipUtilTest {

    @Test
    void testSimpleUnzip() throws IOException {
        File directory = TempZipUtil.tempUnzip(
                new ZipInputStream(
                        this.getClass().getClassLoader().getResourceAsStream("Archive.zip")),
                "tmpedir");
        assertThat(directory).exists();
        assertThat(directory).isDirectory();

        assertThat(Arrays.stream(directory.listFiles()).map(File::getName).toList())
                .containsExactlyInAnyOrder(
                        "1.txt","2.txt","3.txt","4.txt","5.txt"
                );
        assertThat(new File(directory, "5.txt")).hasContent("Zus");
    }

    @Test
    void testUnzipWithDirectories() throws IOException {
        File directory = TempZipUtil.tempUnzip(
                new ZipInputStream(
                        this.getClass().getClassLoader()
                                .getResourceAsStream("Archive2.zip")),
                "tmpedir");
        assertThat(directory).exists();
        assertThat(directory).isDirectory();

        assertThat(Arrays.stream(directory.listFiles()).map(File::getName).toList())
                .containsExactlyInAnyOrder(
                        "1.txt","2.txt","directory"
                );

        File subDirectory = new File(directory, "directory");
        assertThat(Arrays.stream(subDirectory.listFiles()).map(File::getName).toList())
                .containsExactlyInAnyOrder(
                        "3.txt","4.txt","directory"
                );
        File subSubDirectory = new File(subDirectory, "directory");
        assertThat(Arrays.stream(subSubDirectory.listFiles()).map(File::getName).toList())
                .containsExactlyInAnyOrder(
                        "5.txt"
                );

        assertThat(new File(subSubDirectory, "5.txt")).hasContent("Zus");
    }
}