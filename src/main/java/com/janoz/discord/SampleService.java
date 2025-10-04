package com.janoz.discord;

import com.janoz.discord.domain.Sample;
import com.janoz.discord.utils.TempZipUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.zip.ZipInputStream;

@SuppressWarnings("unused")
public interface SampleService
{
    /**
     * Reads and initializes audio samples from the specified zipfile.
     * The method processes files with supported audio formats such as mp3, aac,
     * ogg and wav and updates the sample repository with the loaded samples.
     * <p>
     * If a single audio file contains multiple samples, the file should be accompanied
     * by a metadata file containing at least the sample name, start and length.
     * <p>
     * The zipfile name is prepended to the sampleId's, unless the sample is defined
     * in a metadata file.
     *
     * @param sampleZip the zipfile containing audio sample files
     * @throws IOException
     */
    default void readSamplesZip(String sampleZip) throws IOException {
        readSamplesZip(new File(sampleZip));
    }

    /**
     * Reads and initializes audio samples from the specified zipfile.
     * @see #readSamplesZip(String)
     *
     * @param sampleZip the zipfile containing audio sample files
     * @throws IOException
     */
    default void readSamplesZip(File sampleZip) throws IOException {
        readSamplesZip(sampleZip.getName() + '/', new FileInputStream(sampleZip));
    }

    /**
     * Reads and initializes audio samples from the specified zipfile stream.
     * @see #readSamplesZip(String)
     *
     * @param zipStream InputStream of a zipfile
     * @throws IOException
     */
    default void readSamplesZip(InputStream zipStream) throws IOException {
        readSamplesZip("",zipStream);
    }

    /**
     * Reads and initializes audio samples from the specified zipfile stream.
     * @see #readSamplesZip(String)
     *
     * @param idPrefix prefix to be prepended to the sampleId's
     * @param zipStream InputStream of a zipfile
     * @throws IOException
     */
    default void readSamplesZip(String idPrefix, InputStream zipStream) throws IOException {
        File tempDir = TempZipUtil.tempUnzip(new ZipInputStream(zipStream),"discord-voice-samples");
        readSamples(idPrefix, tempDir);
    }

    /**
     * Reads and initializes audio samples from the specified directory.
     * The method processes files with supported audio formats such as mp3, aac,
     * ogg and wav and updates the sample repository with the loaded samples.
     * <p>
     * If a single audio file contains multiple samples the file should be acompanied
     * by a metadata file containing at least the sample name, start and length.
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
     * If a single audio file contains multiple samples, the file should be
     * accompanied by a metadata file containing at least the sample name,
     * start, and length.
     * <p>
     * The prefix is prepended to the sampleId's, unless the sample is defined
     * in a metadata file .
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
