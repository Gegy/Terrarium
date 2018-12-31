package net.gegy1000.terrarium.api;

import javax.annotation.Nullable;

public interface ExtendedLevelGenerator {
    void setSource(CustomLevelGenerator generator);

    @Nullable
    CustomLevelGenerator getSource();
}
