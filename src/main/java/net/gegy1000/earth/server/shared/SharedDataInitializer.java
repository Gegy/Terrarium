package net.gegy1000.earth.server.shared;

import net.gegy1000.earth.server.util.OpProgressWatcher;

public interface SharedDataInitializer {
    void initialize(SharedEarthData data, OpProgressWatcher progress) throws SharedInitException;

    String getDescription();
}
