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
        Set<File> tmpDirHolder = new HashSet<>();


        cut.readValidator = direcotry -> {
            tmpDirHolder.add(direcotry);
            assertThat(direcotry).exists();
            assertThat(direcotry).isDirectory();

            assertThat(Arrays.stream(direcotry.listFiles()).map(File::getName).toList())
                    .containsExactlyInAnyOrder(
                            "1.txt","2.txt","3.txt","4.txt","5.txt"
                            );
            assertThat(new File(direcotry, "5.txt")).hasContent("Zus");
        };

        cut.readSamplesZip(this.getClass().getClassLoader().getResourceAsStream("Archive.zip"));
    }


    @Test
    void testReadSamples() {
        AtomicBoolean ran = new AtomicBoolean(false);
        cut.readValidator = direcotry -> {
            ran.set(true);
            assertThat(direcotry).isEqualTo(new File("testDirectory"));
        };

        cut.readSamples("testDirectory");

        assertThat(ran).isTrue();
    }


    static class SampleServiceImpl implements SampleService {

        Consumer<File> readValidator;

        @Override
        public void readSamples(String prefix, File sampleDirectory) {
            readValidator.accept(sampleDirectory);
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
