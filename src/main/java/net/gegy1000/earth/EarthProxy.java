package net.gegy1000.earth;

import net.gegy1000.earth.server.message.EarthMapGuiMessage;

public interface EarthProxy {
    default void openMapGui(EarthMapGuiMessage.Type type, double latitude, double longitude) {
    }

    default void displayPanorama() {
    }
}
