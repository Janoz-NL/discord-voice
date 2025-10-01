package com.janoz.discord;

import com.janoz.discord.domain.Sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings("unused")
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
        File f = new File(sampleZip);
        readSamplesZip(f.getName(), new FileInputStream(f));
    }

    /**
     * Reads and initializes audio samples from the specified zipfile stream.
     * @see #readSamplesZip(String)
     *
     * @param zipStream InputStream of an opened zipfile
     */
    default void readSamplesZip(InputStream zipStream) throws IOException {
        readSamplesZip("",zipStream);
    }

    default void readSamplesZip(String idPrefix, InputStream zipStream) throws IOException {
        File tempDir = Files.createTempDirectory("discord-voice-samples").toFile();
        tempDir.deleteOnExit();
        ZipInputStream zis = new ZipInputStream(zipStream);
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            if (zipEntry.isDirectory()) {continue;}
            File file = new File(tempDir, zipEntry.getName());
            if (file.getParentFile().exists()) {
                Files.copy(zis, file.toPath());
                file.deleteOnExit();
            }
            zis.closeEntry();
            zipEntry = zis.getNextEntry();
        }
        readSamples(tempDir.getAbsolutePath());
    }

    /**
     * Reads and initializes audio samples from the specified directory.
     * The method processes files with supported audio formats such as mp3, aac,
     * ogg and wav and updates the sample repository with the loaded samples.
     * <p>
     * If a single audio file contains multiple samples the file should be acompanied
     * by a metadata file containing at least the sample name, start and length.
     * <p>
     *
     * @param sampleDirectory the path to the directory containing audio sample files
     */
    default void readSamples(String sampleDirectory) {
        readSamples("",new File(sampleDirectory));
    }

    /**
     * Reads and initializes audio samples from the specified directory.
     * The method processes files with supported audio formats such as mp3, aac,
     * ogg, and wav and updates the sample repository with the loaded samples.
     * <p>
     * The prefix is added to the sampleId's
     * <p>
     * If a single audio file contains multiple samples, the file should be
     * accompanied by a metadata file containing at least the sample name,
     * start, and length.
     *
     * @param prefix a unique prefix applied to the identifiers of the loaded samples
     * @param sampleDirectory the path to the directory containing audio sample files
     */
    void readSamples(String prefix, File sampleDirectory);

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
    Collection<Sample> getSamples();

    /**
     * Retrieves an audio sample by its unique identifier.
     *
     * @param id the unique identifier of the audio sample to retrieve
     * @return the Sample object corresponding to the specified identifier,
     *         or null if no matching sample is found
     */
    Sample getSample(String id);
}
