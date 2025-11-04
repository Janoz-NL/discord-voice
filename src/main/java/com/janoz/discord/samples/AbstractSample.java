package com.janoz.discord.samples;

import com.janoz.discord.domain.Sample;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class AbstractSample implements Sample {
    private String id;
    private String name;
    private String errorMessage;
    private int start = 0;
    private int length = -1;

    public abstract boolean isValid();

    public boolean isInvalid() {
        return errorMessage!=null;
    }

    public boolean isLoaded() {
        return isValid() && !isInvalid();
    }
}
