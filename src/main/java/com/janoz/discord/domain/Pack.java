package com.janoz.discord.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public class Pack {
    private final String name;
    private final  String info;
    private final  Collection<Sample> samples = new ArrayList<>();


    public Pack(String id) {
        this.name = id
                .replace('/', ' ')
                .replace('\\', ' ')
                .trim();
        this.info = "Samples from " + (id.isEmpty() ? "root" : id);
    }

    public Pack(String name, String info) {
        this.name = name;
        this.info = info;
    }

}
