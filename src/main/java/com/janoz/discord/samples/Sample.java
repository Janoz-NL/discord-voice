package com.janoz.discord.samples;


import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Sample implements com.janoz.discord.domain.Sample {

    private String id;
    private String name;
    private AudioTrack sample;
    private String errorMessage;
    private int start = 0;
    private int length = -1;

    public boolean isValid() {
        return sample!=null;
    }

    public boolean isInvalid() {
        return errorMessage!=null;
    }

    public boolean isLoaded() {
        return isValid() && !isInvalid();
    }

    public AudioTrack getSample() {
        AudioTrack clone = this.sample.makeClone();
        clone.setPosition(start);
        return clone;
    }
}
