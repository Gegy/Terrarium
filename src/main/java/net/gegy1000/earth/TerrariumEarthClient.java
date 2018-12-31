package net.gegy1000.earth;

import net.fabricmc.api.ClientModInitializer;
import net.gegy1000.earth.client.gui.EarthLocateGui;
import net.gegy1000.earth.client.gui.EarthTeleportGui;
import net.gegy1000.earth.client.render.PanoramaLookupHandler;
import net.gegy1000.earth.server.message.EarthMapGuiMessage;
import net.gegy1000.earth.server.world.EarthGeneratorConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableTextComponent;

public class TerrariumEarthClient implements ClientModInitializer, EarthProxy {
    public TerrariumEarthClient() {
        TerrariumEarth.proxy = this;
    }

    @Override
    public void onInitializeClient() {
    }

    @Override
    public void openMapGui(EarthMapGuiMessage.Type type, double latitude, double longitude) {
        MinecraftClient client = MinecraftClient.getInstance();
        switch (type) {
            case LOCATE:
                client.openGui(new EarthLocateGui(latitude, longitude));
                break;
            case TELEPORT:
                client.openGui(new EarthTeleportGui(latitude, longitude));
        }
    }

    @Override
    public void displayPanorama() {
        MinecraftClient client = MinecraftClient.getInstance();

        EarthGeneratorConfig config = EarthGeneratorConfig.get(client.world);
        if (config != null) {
            PlayerEntity player = client.player;
            player.addChatMessage(new TranslatableTextComponent("status.earth.panorama.searching"), true);

            double latitude = config.getLatitude(player.x, player.z);
            double longitude = config.getLongitude(player.x, player.z);

            Thread thread = new Thread(() -> {
                try {
                    PanoramaLookupHandler.Result result = PanoramaLookupHandler.queryPanorama(latitude, longitude);
                    if (result != null) {
                        MinecraftClient.getInstance().execute(() -> TerrariumEarthClient.this.setPanoramaState(config, player, result));
                    } else {
                        player.addChatMessage(new TranslatableTextComponent("status.earth.panorama.none_found"), true);
                    }
                } catch (Exception e) {
                    TerrariumEarth.LOGGER.error("Failed to lookup panorama", e);
                    player.addChatMessage(new TranslatableTextComponent("status.earth.panorama.error"), true);
                }
            });
            thread.setDaemon(true);
            thread.setName("Panorama Lookup");
            thread.start();
        }
    }

    private void setPanoramaState(EarthGeneratorConfig config, PlayerEntity player, PanoramaLookupHandler.Result result) {
        double blockX = config.getX(result.getLatitude(), result.getLongitude());
        double blockZ = config.getZ(result.getLatitude(), result.getLongitude());
        double deltaX = player.x - blockX;
        double deltaZ = player.z - blockZ;
        // TODO
        /*if (deltaX * deltaX + deltaZ * deltaZ < PanoramaHandler.IMMERSION_MIN_DISTANCE) {
            player.addChatMessage(new TranslatableTextComponent("status.earth.panorama.immersed"), true);
            PanoramaHandler.setState(new PanoramaHandler.Immersed(result.getId(), blockX, player.y, blockZ));
        } else {
            player.addChatMessage(new TranslatableTextComponent("status.earth.panorama.found"), true);
            PanoramaHandler.setState(new PanoramaHandler.Located(result.getId(), result.getLatitude(), result.getLongitude()));
        }*/
    }
}
