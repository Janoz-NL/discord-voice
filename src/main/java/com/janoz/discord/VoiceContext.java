package com.janoz.discord;

import net.dv8tion.jda.api.JDA;

@SuppressWarnings("unused")
public interface VoiceContext {
    SampleService getSampleService();
    DiscordService getDiscordService();
    JDA getJda();
}
