package com.janoz.discord.discord;

import com.janoz.discord.samples.Sample;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class VoiceConnection {

    private final AudioPlayer player;
    private final AudioManager audioManager;

    @Getter
    private long lastInteraction;

    private Thread timer;

    public VoiceConnection(Guild guild, AudioPlayerManager playerManager) {
        audioManager = guild.getAudioManager();
        player = playerManager.createPlayer();
        audioManager.setSendingHandler(new AudioPlayerSendHandler(player));
        lastInteraction = System.currentTimeMillis();
    }

    /**
     * @return true if connected, false if already connected to this channel
     */
    public boolean connect(long voiceChannelId) {
        if (connectedToThisVoiceChannel(voiceChannelId)) {
            return false;
        }
        if (connectedToOtherVoiceChannel(voiceChannelId)) {
            disconnect();
        }
        VoiceChannel channel = Objects.requireNonNull(audioManager.getGuild().getVoiceChannelById(voiceChannelId));
        audioManager.openAudioConnection(channel);
        return true;
    }

    public boolean connectedToThisVoiceChannel(long voiceChannelId) {
        return Optional.ofNullable(audioManager.getConnectedChannel())
                .filter(cvc -> cvc.getIdLong() == voiceChannelId)
                .isPresent();
    }

    boolean connectedToOtherVoiceChannel(long voiceChannelId) {
        return Optional.ofNullable(audioManager.getConnectedChannel())
                .filter(cvc -> cvc.getIdLong() != voiceChannelId)
                .isPresent();
    }

    public void disconnect() {
        log.info("Disconnecting...");
        stop();
        audioManager.closeAudioConnection();
    }

    public boolean isConnected() {
        return audioManager.isConnected();
    }

    public VoiceChannel getActiveConnection() {
        return Optional.ofNullable(audioManager.getConnectedChannel())
                .map(AudioChannelUnion::asVoiceChannel)
                .orElse(null);
    }

    public void play(Sample sample) {
        if ((sample != null) && (sample.isValid())) {
            if (timer != null) {
                timer.interrupt();
                timer = null;
            }
            player.stopTrack();
            player.startTrack(sample.getSample(),true);
            if (sample.getLenght()>0) {
                timer = new Thread(() -> {
                    try {
                        Thread.sleep(sample.getLenght());
                        player.stopTrack();
                    } catch (InterruptedException e) {
                        log.debug("Timer interrupted");
                    }
                });
                timer.start();
            }
        }
        lastInteraction = System.currentTimeMillis();
    }

    public void stop() {
        player.stopTrack();
    }
}
