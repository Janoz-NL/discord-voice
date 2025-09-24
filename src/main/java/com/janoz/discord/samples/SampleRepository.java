package com.janoz.discord.samples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
public class SampleRepository {

    private final AudioPlayerManager audioPlayerManager;

    private final Map<String, Sample> samples = new HashMap<>();

    private final Map<String, Pack> packs = new HashMap<>();


    public Sample getSample(String id) {
        return samples.get(id);
    }

    public Collection<Sample> getSamples() {
        return Collections.unmodifiableCollection(samples.values());
    }

    public Collection<Pack> getPacks() {
        return Collections.unmodifiableCollection(packs.values());
    }

    public void clear() {
        samples.clear();
        packs.clear();
    }

    public void readSamples(String sampleDirectory, Runnable afterLoaded) {
        log.info("Loading samples from {}.", sampleDirectory);

        SampleLoaderSemaphore semaphore = new SampleLoaderSemaphore(afterLoaded);
        semaphore.inc();

        File directory = new File(sampleDirectory);
        if (directory.exists() && directory.isDirectory()) {
            Arrays.stream(Objects.requireNonNull(directory
                            .listFiles((file, s) ->
                                    s.toLowerCase(Locale.ROOT).endsWith(".mp3") ||
                                    s.toLowerCase(Locale.ROOT).endsWith(".ogg") ||
                                    s.toLowerCase(Locale.ROOT).endsWith(".aac") ||
                                    s.toLowerCase(Locale.ROOT).endsWith(".wav")
                            )))
                            .forEach(f -> this.read(f,semaphore));
            log.info("{} samples loaded from '{}'.", samples.size(), sampleDirectory);
            semaphore.dec();
        } else {
            log.error("'{}' doesn't exist or isn't a directory. No samples loaded!", sampleDirectory);
            throw new IllegalStateException("Unable to initialize sample repository");
        }
   }
   
    private void read(File file, SampleLoaderSemaphore semaphore) {
        Collection<Sample> samples = readMetadata(file).orElseGet( () -> {
            Sample s = new Sample();
            s.setName(makeNice(file.getName()));
            s.setId(file.getName());
            return Collections.singleton(s);
        });
        semaphore.inc();
        audioPlayerManager.loadItem(file.getAbsolutePath(), new MyLoadResultHanlder(samples, semaphore));
        samples.forEach(s -> this.samples.put(s.getId(), s));
    }

    @SneakyThrows
    private Optional<Collection<Sample>> readMetadata(File file) {
        File metadataFile = new File(file.getAbsolutePath() + ".json");
        if (metadataFile.exists() && metadataFile.isFile()) {
            Collection<Sample> samples = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(metadataFile);
            String packName = rootNode.get("name").asText();
            log.info("Reading " + packName);
            String info = rootNode.optional("info").map(JsonNode::asText).orElse(null);
            String mainId = rootNode.optional("id")
                    .map(JsonNode::asText)
                    .orElse(""+packName.hashCode());
            Pack pack = Pack.builder()
                    .id(mainId)
                    .name(packName)
                    .info(info)
                    .build();
            packs.put(pack.getId(), pack);
            for (JsonNode jsonSample : rootNode.get("samples")) {
                Sample sample = new Sample();
                sample.setPack(pack);
                sample.setName(jsonSample.get("name").asText());
                sample.setId(mainId + "|" +
                        jsonSample
                                .optional("id")
                                .map(JsonNode::asText)
                                .orElse(""+sample.getName().hashCode()));
                sample.setStart(jsonSample.get("position").asInt());
                sample.setLenght(jsonSample.get("length").asInt());
                samples.add(sample);
            }
            return Optional.of(samples);
        }
        return Optional.empty();
    }

    private static String makeNice(String input) {
        return input.substring(0,input.lastIndexOf(".")).replaceAll("[^a-zA-Z0-9/-]"," ");
    }


    private record MyLoadResultHanlder(Collection<Sample> samples, SampleLoaderSemaphore semaphore) implements AudioLoadResultHandler {

        @Override
        public void trackLoaded(AudioTrack audioTrack) {
            samples.forEach(s -> s.setSample(audioTrack));
            semaphore.dec();
        }

        @Override
        public void playlistLoaded(AudioPlaylist audioPlaylist) {
            samples.forEach(s -> s.setErrorMessage("Playlists not supported."));
            semaphore.dec();
        }

        @Override
        public void noMatches() {
            samples.forEach(s -> s.setErrorMessage("Sample not found."));
            semaphore.dec();
        }

        @Override
        public void loadFailed(FriendlyException e) {
            samples.forEach(s -> s.setErrorMessage(e.getMessage()));
            semaphore.dec();
        }
    }

    private static final class SampleLoaderSemaphore {
        private final AtomicInteger i = new AtomicInteger(0);
        private final Runnable whenDone;

        SampleLoaderSemaphore(Runnable whenDone) {
            this.whenDone = whenDone;
        }

        void inc() {
            i.incrementAndGet();
        }

        void dec() {
            if (i.decrementAndGet() == 0) {
                whenDone.run();
            }
         }
    }
}
