package com.janoz.discord.samples;


import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Sample implements com.janoz.discord.domain.Sample {

    private Pack pack;
    private String id;
    private String name;
    private AudioTrack sample;
    private String errorMessage;
    private int start = 0;
    private int lenght = -1;

    public boolean isValid() {
        return sample!=null;
    }

    public boolean isInvalid() {
        return errorMessage!=null;
    }

    public AudioTrack getSample() {
        AudioTrack sample = this.sample.makeClone();
        sample.setPosition(start);
        return sample;
    }

    public void setPack(Pack pack) {
        this.pack = pack;
        pack.getSamples().add(this);
    }
}
