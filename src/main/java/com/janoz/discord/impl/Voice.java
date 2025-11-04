package com.janoz.discord.impl;

import com.janoz.discord.DiscordService;
import com.janoz.discord.SampleService;
import com.janoz.discord.VoiceContext;
import com.janoz.discord.domain.Activity;
import com.janoz.discord.domain.Guild;
import com.janoz.discord.domain.Pack;
import com.janoz.discord.domain.Sample;
import com.janoz.discord.domain.VoiceChannel;
import com.janoz.discord.discord.DiscordSample;
import com.janoz.discord.discord.DiscordSampleLoader;
import com.janoz.discord.samples.SampleRepository;
import com.janoz.discord.discord.BotManager;
import com.janoz.discord.discord.VoiceConnectionService;
import com.janoz.discord.tasks.DisconnectingTask;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Slf4j
public class Voice implements SampleService, DiscordService, VoiceContext {

    @Getter // at least make JDA available to the outside
    private final JDA jda;
    private final SampleRepository<DiscordSample> sampleRepository;
    private final BotManager botManager;
    private final VoiceConnectionService voiceConnectionService;

    private Voice(JDA jda, Duration disconnectAfter) {
        this.jda = jda;
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(playerManager);
        sampleRepository = new SampleRepository<>(new DiscordSampleLoader(playerManager));
        botManager = new BotManager(jda);
        voiceConnectionService = new VoiceConnectionService(jda, playerManager);
        Optional.ofNullable(disconnectAfter).ifPresent(d ->
            DisconnectingTask.startRunning(voiceConnectionService, d));
    }

    @Override
    public DiscordService getDiscordService() {
        return this;
    }

    @Override
    public SampleService getSampleService() {
        return this;
    }

    @Override
    public void setBotActivity(Activity activity) {
        botManager.setActivity(activity);
    }

    @Override
    public Activity getBotActivity() {
        return botManager.getActivity();
    }

    @Override
    public void readSamples(String prefix, File sampleDirectory) {
        sampleRepository.readSamples(prefix, sampleDirectory);
    }

    @Override
    public void clearSamples() {
        sampleRepository.clear();
    }

    @Override
    public Collection<Sample> getSamples() {
        return sampleRepository.getSamples().stream()
                .map(s -> (Sample)s)
                .toList();
    }

    @Override
    public Collection<Pack> getPacks() {
        return sampleRepository.getPacks();
    }

    @Override
    public Sample getSample(String id) {
        return sampleRepository.getSample(id);
    }

    @Override
    public Collection<Guild> getGuilds() {
        return jda.getGuilds().stream().map(Voice::getGuild).toList();
    }

    private static Guild getGuild(net.dv8tion.jda.api.entities.Guild jdag) {
        Guild guild = Guild.builder()
                .id(jdag.getIdLong())
                .name(jdag.getName())
                .build();
        jdag.getVoiceChannels().forEach(jdavc -> {
            VoiceChannel vc = VoiceChannel.builder()
                    .guild(guild)
                    .id(jdavc.getIdLong())
                    .name(jdavc.getName())
                    .connected(jdavc == jdavc.getGuild().getAudioManager().getConnectedChannel())
                    .build();
            guild.getVoiceChannels().add(vc);
        });
        return guild;
    }

    @Override
    public Guild getGuild(long guildId) {
        return Optional.ofNullable(jda.getGuildById(guildId))
                .map(Voice::getGuild)
                .orElse(null);
    }

    @Override
    public VoiceChannel getVoiceChannel(long channelId) {
        return Optional.ofNullable(jda.getChannelById(GuildChannel.class, channelId))
                .map(GuildChannel::getGuild)
                .map(Voice::getGuild)
                .map(Guild::getVoiceChannels)
                .orElse(Collections.emptyList())
                .stream()
                .filter(vc -> vc.getId() == channelId)
                .findAny().orElse(null);
    }

    @Override
    public boolean connect(long guildId, long voiceChannelId) {
        return voiceConnectionService.getConnection(guildId).connect(voiceChannelId);
    }

    @Override
    public void disconnect(long guildId) {
        voiceConnectionService.getConnection(guildId).disconnect();
    }

    @Override
    public void play(String sampleId, long guildId) {
        voiceConnectionService.getConnection(guildId).play(sampleRepository.getSample(sampleId));
    }

    @Override
    public void play(String sampleId, long guildId, long voiceChannelId) {
        if (voiceConnectionService.getConnection(guildId).connect(voiceChannelId)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        voiceConnectionService.getConnection(guildId).play(sampleRepository.getSample(sampleId));
    }

    private static JDA initJDA(String token) {
        log.info("Connecting to Discord using token '**..**{}'", token.substring(token.length()-6));
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE, CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOJI);
        builder.setBulkDeleteSplittingEnabled(false);
        builder.setCompression(Compression.NONE);
        JDA jda = builder.build();
        try {
            jda.awaitReady();
            log.info("Connected to Discord");
        } catch (InterruptedException e) {
            log.info("Interrupted while connecting");
            Thread.currentThread().interrupt();
        }
        return jda;
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private JDA jda;
        private Duration disconnectAfter = Duration.ofMinutes(15);
        private boolean mock = false;

        public Builder jda(JDA jda) {
            this.jda = jda;
            return this;
        }

        public Builder token(String token) {
            this.jda = initJDA(token);
            return this;
        }

        public Builder disconnectAfter(Duration disconnectAfter) {
            this.disconnectAfter = disconnectAfter;
            return this;
        }

        public Builder withoutAutoDisconnect() {
            this.disconnectAfter = null;
            return this;
        }

        public Builder asMock() {
            mock = true;
            return this;
        }

        public VoiceContext build() {
            return mock? new VoiceMock():new Voice(jda,  disconnectAfter);
        }
    }
}
