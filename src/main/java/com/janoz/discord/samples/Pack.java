package com.janoz.discord.samples;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Builder
public class Pack {
    private final String id;
    private final String name;
    private final String info;
    private final Collection<Sample> samples = new ArrayList<>();
}
