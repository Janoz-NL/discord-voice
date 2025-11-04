package com.janoz.discord.discord;

import com.janoz.discord.samples.AbstractSample;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DiscordSample extends AbstractSample {

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
