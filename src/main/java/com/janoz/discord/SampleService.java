package com.janoz.discord;

import com.janoz.discord.domain.Sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public interface SampleService
{
    /**
     * Reads and initializes audio samples from the specified zipfile.
     * The method processes files with supported audio formats such as mp3, aac,
     * ogg and wav and updates the sample repository with the loaded samples.
     * <p>
     * If a single audio file contains multiple samples the file should be acompanied
     * by a metadata file containing at least the sample name, start and length.
     * <p>
     * Subdirectories are not supported
     *
     * @param sampleZip the zipfile containing audio sample files
     */
    default void readSamplesZip(String sampleZip) throws IOException {
        File tempDir = Files.createTempDirectory("discord-voic-samples").toFile();
        tempDir.deleteOnExit();
        System.out.println(tempDir.getAbsolutePath());
        ZipInputStream zis = new ZipInputStream(new FileInputStream(sampleZip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            if (zipEntry.isDirectory()) {continue;}
            File file = new File(tempDir, zipEntry.getName());
            if (!file.getParentFile().exists()) {
                System.out.println("File not in root ignored " + zipEntry.getName());
            }
            Files.copy(zis, file.toPath());
            zis.closeEntry();
            file.deleteOnExit();
            zipEntry = zis.getNextEntry();
        }
        readSamples(tempDir.getAbsolutePath(), () -> {
            for(File file : tempDir.listFiles()) {
                file.delete();
            }
            tempDir.delete();
        });
    }

    /**
     * Reads and initializes audio samples from the specified directory.
     * The method processes files with supported audio formats such as mp3, aac,
     * ogg and wav and updates the sample repository with the loaded samples.
     * <p>
     * If a single audio file contains multiple samples the file should be acompanied
     * by a metadata file containing at least the sample name, start and length.
     * <p>
     * Subdirectories are not supported
     *
     * @param sampleDirectory the path to the directory containing audio sample files
     */
    default void readSamples(String sampleDirectory) {
        readSamples(sampleDirectory, () -> {});
    }

    void readSamples(String sampleDirectory, Runnable afterLoaded);

    /**
     * Clears all audio samples from the sample repository.
     * This method removes all audio samples currently stored in the repository,
     * effectively resetting the sample repository to an empty state.
     */
    void clearSamples();

    /**
     * Retrieves a list of audio samples currently managed by the system.
     *
     * @return a list of Sample objects representing the available audio samples
     */
    List<Sample> getSamples();

    /**
     * Retrieves an audio sample by its unique identifier.
     *
     * @param id the unique identifier of the audio sample to retrieve
     * @return the Sample object corresponding to the specified identifier,
     *         or null if no matching sample is found
     */
    Sample getSample(String id);
}
