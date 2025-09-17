package com.janoz.discord.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Builder
public class Guild {
    private final long id;
    private final String name;
    private final Collection<VoiceChannel> voiceChannels = new ArrayList<>();
}
