package com.janoz.discord.samples.impl;

import java.io.File;
import java.util.Collection;

public abstract class AbstractSampleLoader<T extends AbstractSample> {

    public abstract void loadSample(File file, Collection<T> newSamples);

    public abstract T createSampleObject();
}
