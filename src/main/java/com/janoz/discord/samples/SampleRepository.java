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

@Slf4j
@RequiredArgsConstructor
public class SampleRepository {

    private final AudioPlayerManager audioPlayerManager;

    private final Map<String, Sample> samples = new HashMap<>();

    public Sample getSample(String id) {
        return samples.get(id);
    }

    public Collection<Sample> getSamples() {
        return Collections.unmodifiableCollection(samples.values());
    }

    public void clear() {
        samples.clear();
    }

    public void readSamples(String prefix, File sampleDirectory) {
        log.info("Loading samples from {}.", sampleDirectory);
        if (sampleDirectory.exists() && sampleDirectory.isDirectory()) {
            int oldCount = samples.size();
            Arrays.stream(Objects.requireNonNull(
                    sampleDirectory.listFiles((dir, name) ->
                                name.toLowerCase(Locale.ROOT).endsWith(".mp3") ||
                                name.toLowerCase(Locale.ROOT).endsWith(".ogg") ||
                                name.toLowerCase(Locale.ROOT).endsWith(".aac") ||
                                name.toLowerCase(Locale.ROOT).endsWith(".wav"))))
                        .forEach(f -> this.read(prefix, f));
            log.info("{} samples loaded from '{}'.",
                    samples.size() - oldCount,
                    sampleDirectory.getAbsolutePath());
            //must go deeper
            Arrays.stream(Objects.requireNonNull(
                    sampleDirectory.listFiles((dir, name) ->
                               new File(dir,name).isDirectory())))
                        .forEach(f -> readSamples(prefix + f.getName() + '/', f));
        } else {
            log.error("'{}' doesn't exist or isn't a directory. No samples loaded!", sampleDirectory);
            throw new IllegalStateException("Unable to initialize sample repository");
        }
   }
   
    private void read(String prefix, File file) {
        Collection<Sample> newSamples = readMetadata(file).orElseGet( () -> {
            Sample s = new Sample();
            s.setName(makeNice(file.getName()));
            s.setId(prefix + file.getName());
            return Collections.singleton(s);
        });
        audioPlayerManager.loadItem(file.getAbsolutePath(), new MyLoadResultHanlder(newSamples));
        newSamples.forEach(s -> this.samples.put(s.getId(), s));
    }

    /**
     * When a metadatafile is found the metadata determines the metadata of the sample.
     * Other conventions, like relative filname and path as Id and cleaned filename as
     * sample name, are ignored.
     *
     * @param file an audio file potentially containing multiple samples
     * @return Collection of samples when a metadata file was found, otherwise empty
     */
    @SneakyThrows
    private Optional<Collection<Sample>> readMetadata(File file) {
        File metadataFile = new File(file.getAbsolutePath() + ".json");
        if (metadataFile.exists() && metadataFile.isFile()) {
            Collection<Sample> newSamples = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(metadataFile);
            String packName = rootNode.get("name").asText();
            log.info("Reading " + packName);
            String mainId = rootNode.optional("id")
                    .map(JsonNode::asText)
                    .orElse(""+packName.hashCode());
            for (JsonNode jsonSample : rootNode.get("samples")) {
                Sample sample = new Sample();
                sample.setName(jsonSample.get("name").asText());
                sample.setId(mainId + "|" +
                        jsonSample
                                .optional("id")
                                .map(JsonNode::asText)
                                .orElse(""+sample.getName().hashCode()));
                sample.setStart(jsonSample.get("position").asInt());
                sample.setLength(jsonSample.get("length").asInt());
                newSamples.add(sample);
            }
            return Optional.of(newSamples);
        }
        return Optional.empty();
    }

    private static String makeNice(String input) {
        return input.substring(0,input.lastIndexOf(".")).replaceAll("[^a-zA-Z0-9/-]"," ");
    }


    private record MyLoadResultHanlder(Collection<Sample> samples) implements AudioLoadResultHandler {

        @Override
        public void trackLoaded(AudioTrack audioTrack) {
            samples.forEach(s -> s.setSample(audioTrack));
        }

        @Override
        public void playlistLoaded(AudioPlaylist audioPlaylist) {
            samples.forEach(s -> s.setErrorMessage("Playlists not supported."));
        }

        @Override
        public void noMatches() {
            samples.forEach(s -> s.setErrorMessage("Sample not found."));
        }

        @Override
        public void loadFailed(FriendlyException e) {
            samples.forEach(s -> s.setErrorMessage(e.getMessage()));
        }
    }
}
