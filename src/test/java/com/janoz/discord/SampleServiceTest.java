package com.janoz.discord;

import com.janoz.discord.domain.Sample;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class SampleServiceTest {

    SampleServiceImpl cut = new SampleServiceImpl();

    @Test
    void testReadSamplesZip() throws IOException{
        Set<String> tmpDirHolder = new HashSet<>();


        cut.readValidator = direcotry -> {
            tmpDirHolder.add(direcotry);
            File f = new File(direcotry);
            assertThat(f).exists();
            assertThat(f).isDirectory();

            assertThat(Arrays.stream(f.listFiles()).map(File::getName).toList())
                    .containsExactlyInAnyOrder(
                            "1.txt","2.txt","3.txt","4.txt","5.txt"
                            );
            assertThat(new File(f, "5.txt")).hasContent("Zus");
        };

        cut.readSamplesZip("testData/Archive.zip");

        File f = new File(tmpDirHolder.iterator().next());
        assertThat(f).doesNotExist();
    }


    @Test
    void testReadSamples() {
        AtomicBoolean ran = new AtomicBoolean(false);
        cut.readValidator = direcotry -> {
            ran.set(true);
            assertThat(direcotry).isEqualTo("testDirectory");
        };

        cut.readSamples("testDirectory");

        assertThat(ran).isTrue();
    }


    static class SampleServiceImpl implements SampleService {

        Consumer<String> readValidator;

        @Override
        public void readSamples(String sampleDirectory, Runnable afterLoaded) {
            readValidator.accept(sampleDirectory);
            afterLoaded.run();
        }

        @Override
        public void clearSamples() {

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
