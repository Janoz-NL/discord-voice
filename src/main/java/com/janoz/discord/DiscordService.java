package com.janoz.discord;

import com.janoz.discord.domain.Activity;
import com.janoz.discord.domain.Guild;
import com.janoz.discord.domain.Sample;
import com.janoz.discord.domain.VoiceChannel;
import net.dv8tion.jda.api.JDA;

import java.util.Collection;

@SuppressWarnings("unused")
public interface DiscordService {
    /**
     * Sets the bot's activity to the specified activity.
     * The activity defines the type and the name that will
     * be displayed as the bot's current status.
     *
     * @param activity the activity to set for the bot, which includes the activity type and its name
     */
    void setBotActivity(Activity activity);

    /**
     * Retrieves the current activity of the bot.
     * The activity represents the status and activity type
     * that the bot is currently displaying.
     *
     * @return the current Activity of the bot, containing its type and name
     */
    Activity getBotActivity();

    /**
     * Retrieves a collection of Guild objects, each representing a Discord guild (server) that the bot has access to.
     * The Guild objects include their respective voice channels.
     *
     * @return a collection of Guild objects, each populated with its details and associated voice channels
     */
    Collection<Guild> getGuilds();

    /**
     * Connects to the specified voice channel. If the bot is already connected to another
     * voice channel in the same guild, it will disconnect from the current one and connect
     * to the specified channel.
     *
     * @param voiceChannel the voice channel to connect to
     * @return true if the connection was successful, or false if the bot was already connected to the specified channel
     */
    default boolean connect(VoiceChannel voiceChannel) {
        long guildId = voiceChannel.getGuild().getId();
        long voiceChannelId = voiceChannel.getId();
        return connect(guildId, voiceChannelId);
    }

    boolean connect(long guildId, long voiceChannelId);

    /**
     * Disconnects the bot from the voice channel in the specified guild.
     *
     * @param guild the guild from which the bot should disconnect its voice connection
     */
    default void disconnect(Guild guild) {
        disconnect(guild.getId());
    }

    void disconnect(long guildId);

    /**
     * Plays the specified audio sample in the given guild's voice channel.
     *
     * @param sample the audio sample to be played
     * @param guild the guild where the sample will be played
     */
    default void play(Sample sample, Guild guild) {
        String sampleId = sample.getId();
        long guildId = guild.getId();
        play(sampleId, guildId);
    }

    /**
     * Plays the specified audio sample in the given guild's voice channel.
     *
     * @param sampleId the id of the audio sample to be played
     * @param guildId the id of the guild where the sample will be played
     */
    void play(String sampleId, long guildId);

    /**
     * Plays an audio sample in the specified voice channel.
     * If the bot is not already connected to the voice channel,
     * it will attempt to connect before playing.
     *
     * @param sample the audio sample to be played
     * @param voiceChannel the voice channel where the sample will be played
     */
    default void play(Sample sample, VoiceChannel voiceChannel) {
        String sampleId = sample.getId();
        long guildId = voiceChannel.getGuild().getId();
        long voiceChannelId = voiceChannel.getId();
        play(sampleId, guildId, voiceChannelId);
    }

    /**
     * Plays an audio sample in the specified voice channel.
     * If the bot is not already connected to the voice channel,
     * it will attempt to connect before playing.
     *
     * @param sampleId the id of the audio sample to be played
     * @param voiceChannelId the id of the voice channel where the sample will be played
     */
    void play(String sampleId, long guildId, long voiceChannelId);

    JDA getJda();
}
