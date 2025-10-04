package com.janoz.discord.impl;

import com.janoz.discord.DiscordService;
import com.janoz.discord.SampleService;
import com.janoz.discord.VoiceContext;
import com.janoz.discord.domain.Activity;
import com.janoz.discord.domain.Guild;
import com.janoz.discord.domain.Sample;
import com.janoz.discord.domain.VoiceChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;

import java.io.File;
import java.util.Collection;
import java.util.List;

@Slf4j
public class VoiceMock implements SampleService, DiscordService, VoiceContext  {

    private final Guild guild;
    private final VoiceChannel voiceChannel;

    public VoiceMock() {
        guild = Guild.builder()
                .id(1L)
                .name("guild")
                .build();
        voiceChannel = VoiceChannel.builder()
                .guild(guild)
                .id(2L)
                .name("voiceChannel")
                .build();
        guild.getVoiceChannels().add(voiceChannel);
    }

    @Getter @Setter
    private Activity botActivity;

    @Override
    public Collection<Guild> getGuilds() {
        return List.of(guild);
    }

    @Override
    public Guild getGuild(long guildId) {
        return guildId==1?guild:null;
    }

    @Override
    public VoiceChannel getVoiceChannel(long channelId) {
        return channelId==2?voiceChannel:null;
    }

    @Override
    public boolean connect(long guildId, long voiceChannelId) {
        return false;
    }

    @Override
    public void disconnect(long guildId) {
        log.info("Disconnecting from {}",guildId);
    }

    @Override
    public void play(String sampleId, long guildId) {
        log.info("Playing sample {} on guild {}",sampleId, guildId);
    }

    @Override
    public void play(String sampleId, long guildId, long voiceChannelId) {
        log.info("Playing sample {} on guild {} and voicechannel {}",sampleId, guildId, voiceChannelId);

    }

    @Override
    public SampleService getSampleService() {
        return this;
    }

    @Override
    public DiscordService getDiscordService() {
        return this;
    }

    @Override
    public JDA getJda() {
        return null;
    }

    @Override
    public void readSamples(String prefix, File sampleDirectory) {
        log.info("Loading samples with prefix {} from {}.",prefix, sampleDirectory);
    }

    @Override
    public void clearSamples() {
        log.info("Clearing samples.");
    }

    @Override
    public Collection<Sample> getSamples() {
        return List.of();
    }

    @Override
    public Sample getSample(String id) {
        return null;
    }
}
