package net.gegy1000.earth.server;

import net.gegy1000.earth.server.world.pipeline.source.EarthRemoteData;
import net.gegy1000.earth.server.world.pipeline.source.SrtmHeightSource;

public class ServerProxy {
    public void onPreInit() {
        Thread thread = new Thread(() -> {
            EarthRemoteData.loadInfo();
            SrtmHeightSource.loadValidTiles();
        }, "Terrarium Remote Load");
        thread.setDaemon(true);
        thread.start();
    }

    public void onInit() {
    }

    public void onPostInit() {
    }
}
