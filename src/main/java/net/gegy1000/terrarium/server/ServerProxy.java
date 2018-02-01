package net.gegy1000.terrarium.server;

import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.map.source.TerrariumRemoteData;
import net.gegy1000.terrarium.server.map.source.height.HeightSource;

public class ServerProxy {
    public void onPreInit() {
        TerrariumCapabilities.onPreInit();

        Thread thread = new Thread(() -> {
            TerrariumRemoteData.loadInfo();
            HeightSource.loadValidTiles();
        }, "Terrarium Remote Load");
        thread.setDaemon(true);
        thread.start();
    }

    public void onInit() {
    }

    public void onPostInit() {
    }
}
