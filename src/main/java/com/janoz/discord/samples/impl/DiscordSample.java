package com.janoz.discord.samples.impl;

import com.janoz.discord.domain.Sample;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DiscordSample extends AbstractSample implements Sample {

    private AudioTrack sample;

    public boolean isValid() {
        return sample!=null;
    }

    public AudioTrack getSample() {
        AudioTrack clone = this.sample.makeClone();
        clone.setPosition(getStart());
        return clone;
    }
}
