package com.janoz.discord.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VoiceChannel {
    private final long id;
    private final String name;
    private final Guild guild;
    private boolean connected;
}
