package com.janoz.discord;

import com.janoz.discord.domain.Activity;
import com.janoz.discord.domain.Guild;
import com.janoz.discord.domain.Sample;
import com.janoz.discord.domain.VoiceChannel;
import com.janoz.discord.samples.SampleRepository;
import com.janoz.discord.services.BotManager;
import com.janoz.discord.services.VoiceConnectionService;
import com.janoz.discord.tasks.DisconnectingTask;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class Voice {

    @Getter // at least make JDA available to the outside
    private final JDA jda;
    private final SampleRepository sampleRepository;
    private final BotManager botManager;
    private final VoiceConnectionService voiceConnectionService;

    public Voice(JDA jda) {
        this.jda = jda;
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(playerManager);
        sampleRepository = new SampleRepository(playerManager);
        botManager = new BotManager(jda);
        voiceConnectionService = new VoiceConnectionService(jda, playerManager);
        DisconnectingTask.startRunning(voiceConnectionService);
    }

    public Voice(String token) {
        this(initJDA(token));
    }

    /**
     * Sets the bot's activity to the specified activity.
     * The activity defines the type and the name that will
     * be displayed as the bot's current status.
     *
     * @param activity the activity to set for the bot, which includes the activity type and its name
     */
    public void setBotActivity(Activity activity) {
        botManager.setActivity(activity);
    }

    /**
     * Retrieves the current activity of the bot.
     * The activity represents the status and activity type
     * that the bot is currently displaying.
     *
     * @return the current Activity of the bot, containing its type and name
     */
    public Activity getBotActivity() {
        return botManager.getActivity();
    }

    /**
     * Reads and initializes audio samples from the specified folder.
     * The method processes files with supported audio formats such as mp3 and wav
     * and updates the sample repository with the loaded samples.
     *
     * @param sampleFolder the path to the folder containing audio sample files
     */
    public void readSamples(String sampleFolder) {
        sampleRepository.readSamples(sampleFolder);
    }

    /**
     * Remove all audio samples.
     */
    public void clearSamples() {
        sampleRepository.clear();
    }

    /**
     * Retrieves a list of audio samples currently managed by the system.
     *
     * @return a list of Sample objects representing the available audio samples
     */
    public List<Sample> getSamples() {
        return sampleRepository.getSamples().stream()
                .map(s -> (Sample)s)
                .toList();
    }

    public Sample getSample(String id) {
        return sampleRepository.getSample(id);
    }

    /**
     * Retrieves a collection of Guild objects, each representing a Discord guild (server) that the bot has access to.
     * The Guild objects include their respective voice channels.
     *
     * @return a collection of Guild objects, each populated with its details and associated voice channels
     */
    public Collection<Guild> getGuilds() {
        List<Guild> result = new ArrayList<>();
        jda.getGuilds().forEach(jdag -> {
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
            result.add(guild);
        });
        return result;
    }

    /**
     * Connects to the specified voice channel. If the bot is already connected to another
     * voice channel in the same guild, it will disconnect from the current one and connect
     * to the specified channel.
     *
     * @param voiceChannel the voice channel to connect to
     * @return true if the connection was successful, or false if the bot was already connected to the specified channel
     */
    public boolean connect(VoiceChannel voiceChannel) {
        long guildId = voiceChannel.getGuild().getId();
        long voiceChannelId = voiceChannel.getId();
        return connect(guildId, voiceChannelId);
    }

    public boolean connect(long guildId, long voiceChannelId) {
        return voiceConnectionService.getConnection(guildId).connect(voiceChannelId);
    }

    /**
     * Disconnects the bot from the voice channel in the specified guild.
     *
     * @param guild the guild from which the bot should disconnect its voice connection
     */
    public void disconnect(Guild guild) {
        disconnect(guild.getId());
    }

    public void disconnect(long guildId) {
        voiceConnectionService.getConnection(guildId).disconnect();
    }

    /**
     * Plays the specified audio sample in the given guild's voice channel.
     *
     * @param sample the audio sample to be played
     * @param guild the guild where the sample will be played
     */
    public void play(Sample sample, Guild guild) {
        String sampleId = sample.getId();
        long guildId = guild.getId();
        play(sampleId, guildId);
    }

    public void play(String sampleId, long guildId) {
        voiceConnectionService.getConnection(guildId).play(sampleRepository.getSample(sampleId));
    }

    /**
     * Plays an audio sample in the specified voice channel.
     * If the bot is not already connected to the voice channel,
     * it will attempt to connect before playing.
     *
     * @param sample the audio sample to be played
     * @param voiceChannel the voice channel where the sample will be played
     */
    public void play(Sample sample, VoiceChannel voiceChannel) {
        String sampleId = sample.getId();
        long guildId = voiceChannel.getGuild().getId();
        long voiceChannelId = voiceChannel.getId();
        play(sampleId, guildId, voiceChannelId);
    }

    public void play(String sampleId, long guildId, long voiceChannelId) {
        if (voiceConnectionService.getConnection(guildId).connect(voiceChannelId)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //
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
            log.info("Interupted while connecting");
        }
        return jda;
    }
}
