package net.gegy1000.terrarium;

import net.fabricmc.api.ClientModInitializer;
import net.gegy1000.terrarium.client.ClientEventHandler;
import net.gegy1000.terrarium.server.world.pipeline.source.LoadingStateHandler;

public class TerrariumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientEventHandler.register();

        LoadingStateHandler.registerClient();
    }
}
