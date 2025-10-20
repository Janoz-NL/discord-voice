package com.janoz.discord.discord;

import com.janoz.discord.samples.AbstractSampleLoader;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.Collection;

@RequiredArgsConstructor
public class DiscordSampleLoader extends AbstractSampleLoader<DiscordSample> {

    private final AudioPlayerManager audioPlayerManager;

    @Override
    public void loadSample(File file, Collection<DiscordSample> newSamples) {
        audioPlayerManager.loadItem(file.getAbsolutePath(), new MyLoadResultHandler(newSamples));
    }

    @Override
    public DiscordSample createSampleObject() {
        return new DiscordSample();
    }

    private record MyLoadResultHandler(Collection<DiscordSample> samples) implements AudioLoadResultHandler {

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
