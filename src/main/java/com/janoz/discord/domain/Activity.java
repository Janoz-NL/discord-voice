package com.janoz.discord.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Activity {

    private final ActivityType type;
    private final String name;

    public enum ActivityType {
        PLAYING,
        STREAMING,
        LISTENING,
        WATCHING,
        CUSTOM_STATUS,
        COMPETING
    }
}
