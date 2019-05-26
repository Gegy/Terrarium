package net.gegy1000.earth.server.shared;

import net.gegy1000.earth.server.util.ProcessTracker;

public interface SharedDataInitializer {
    void initialize(SharedEarthData data, ProcessTracker processTracker);
}
