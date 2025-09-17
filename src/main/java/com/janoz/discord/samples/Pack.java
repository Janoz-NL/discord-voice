package com.janoz.discord.samples;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class Pack {
    private String id;
    private String name;
    private Collection<Sample> samples;
}
