package com.janoz.discord;

import com.janoz.discord.domain.Sample;
import com.janoz.discord.utils.TempZipUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class SampleServiceTest {

    final SampleServiceImpl cut = new SampleServiceImpl();

    @Captor
    ArgumentCaptor<ZipInputStream> zipInputStreamArgumentCaptor;

    @Test
    void testReadSamplesZip() throws IOException{
        File result = mock(File.class);

        cut.readValidator = (prefix,file) -> {
            assertThat(file).isSameAs(result);
            assertThat(prefix).isEqualTo("Archive.zip/");
        };

        try (MockedStatic<TempZipUtil> ziputilMock = mockStatic(TempZipUtil.class)) {
            ziputilMock.when(() ->
                    TempZipUtil.tempUnzip(any(ZipInputStream.class), eq("discord-voice-samples")))
                    .thenReturn(result);

            cut.readSamplesZip("src/test/resources/Archive.zip");

            ziputilMock.verify(() ->
                    TempZipUtil.tempUnzip(zipInputStreamArgumentCaptor.capture(), eq("discord-voice-samples")));
            assertThat(getFilesFromZip(zipInputStreamArgumentCaptor.getValue()))
                    .containsExactlyInAnyOrder("1.txt", "2.txt", "3.txt", "4.txt", "5.txt");
            ziputilMock.verifyNoMoreInteractions();
        }
    }

    @Test
    void testReadSamplesZipStream() throws IOException{
        File result = mock(File.class);

        cut.readValidator = (s,f) -> {
            assertThat(f).isSameAs(result);
            assertThat(s).isEqualTo("");
        };

        try (MockedStatic<TempZipUtil> ziputilMock = mockStatic(TempZipUtil.class)) {
            ziputilMock.when(() ->
                            TempZipUtil.tempUnzip(any(ZipInputStream.class), eq("discord-voice-samples")))
                    .thenReturn(result);

            cut.readSamplesZip(new FileInputStream("src/test/resources/Archive.zip"));

            ziputilMock.verify(() ->
                    TempZipUtil.tempUnzip(zipInputStreamArgumentCaptor.capture(), eq("discord-voice-samples")));
            assertThat(getFilesFromZip(zipInputStreamArgumentCaptor.getValue()))
                    .containsExactlyInAnyOrder("1.txt", "2.txt", "3.txt", "4.txt", "5.txt");
            ziputilMock.verifyNoMoreInteractions();
        }
    }

    @Test
    void testReadSamples() {
        AtomicBoolean ran = new AtomicBoolean(false);
        cut.readValidator = (prefix,directory) -> {
            ran.set(true);
            assertThat(directory).isEqualTo(new File("testDirectory"));
            assertThat(prefix).isEqualTo("");
        };
        cut.readSamples("testDirectory");
        assertThat(ran).isTrue();
    }


    private Set<String> getFilesFromZip(ZipInputStream zipInputStream) throws IOException {
        Set<String> result = new HashSet<>();
        for (ZipEntry zipEntry = zipInputStream.getNextEntry(); zipEntry != null; zipEntry = zipInputStream.getNextEntry()) {
            result.add(zipEntry.getName());
        }
        return result;
    }


    static class SampleServiceImpl implements SampleService {

        BiConsumer<String, File> readValidator;

        @Override
        public void readSamples(String prefix, File sampleDirectory) {
            readValidator.accept(prefix, sampleDirectory);
        }

        @Override
        public void clearSamples() {
            //intentionally left blank
        }

        @Override
        public List<Sample> getSamples() {
            return List.of();
        }

        @Override
        public Sample getSample(String id) {
            return null;
        }
    }
}
