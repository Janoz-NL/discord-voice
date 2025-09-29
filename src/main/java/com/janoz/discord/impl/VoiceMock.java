package com.janoz.discord.impl;

import com.janoz.discord.DiscordService;
import com.janoz.discord.SampleService;
import com.janoz.discord.VoiceContext;
import com.janoz.discord.domain.Activity;
import com.janoz.discord.domain.Guild;
import com.janoz.discord.domain.Sample;
import com.janoz.discord.samples.Pack;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;

import java.util.Collection;
import java.util.List;

public class VoiceMock implements SampleService, DiscordService, VoiceContext  {

    @Getter @Setter
    private Activity botActivity;

    @Override
    public Collection<Guild> getGuilds() {
        return List.of();
    }

    @Override
    public boolean connect(long guildId, long voiceChannelId) {
        return false;
    }

    @Override
    public void disconnect(long guildId) {

    }

    @Override
    public void play(String sampleId, long guildId) {

    }

    @Override
    public void play(String sampleId, long guildId, long voiceChannelId) {

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
    public void readSamples(String sampleDirectory, Runnable afterLoaded) {

    }

    @Override
    public void clearSamples() {

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
