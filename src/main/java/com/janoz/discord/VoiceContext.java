package com.janoz.discord;

import com.janoz.discord.impl.Voice;
import net.dv8tion.jda.api.JDA;

@SuppressWarnings("unused")
public interface VoiceContext {
    SampleService getSampleService();
    DiscordService getDiscordService();
    JDA getJda();

    static Voice.Builder builder() {
        return new Voice.Builder();
    }
}
