package net.gegy1000.earth.client;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.client.gui.EarthLocateGui;
import net.gegy1000.earth.client.gui.EarthPreloadGui;
import net.gegy1000.earth.client.gui.EarthPreloadProgressGui;
import net.gegy1000.earth.client.render.LoadingScreenOverlay;
import net.gegy1000.earth.client.render.PanoramaHandler;
import net.gegy1000.earth.server.ServerProxy;
import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.message.EarthOpenMapMessage;
import net.gegy1000.earth.server.world.data.GooglePanorama;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends ServerProxy {
    @Override
    public void onPostInit() {
        LoadingScreenOverlay.onPostInit();
        LoadingWorldGetter.onPostInit();
    }

    @Override
    public void openDownload(long count, long total) {
        Minecraft client = Minecraft.getMinecraft();
        if (client.currentScreen == null) {
            client.displayGuiScreen(new EarthPreloadProgressGui(count, total));
        }
    }

    @Override
    public void updateDownload(long count) {
        Minecraft client = Minecraft.getMinecraft();

        if (client.currentScreen instanceof EarthPreloadProgressGui) {
            ((EarthPreloadProgressGui) client.currentScreen).update(count);
        }
    }

    @Override
    public void openMapGui(EarthOpenMapMessage.Type type, double latitude, double longitude) {
        Minecraft client = Minecraft.getMinecraft();
        switch (type) {
            case LOCATE:
                client.displayGuiScreen(new EarthLocateGui(latitude, longitude));
                break;
            case PRELOAD:
                EarthWorld earth = EarthWorld.get(client.world);
                if (earth != null) {
                    client.displayGuiScreen(new EarthPreloadGui(earth, latitude, longitude));
                }
                break;
        }
    }

    @Override
    public void displayPanorama() {
        Minecraft mc = Minecraft.getMinecraft();

        World world = mc.world;
        EarthWorld earth = world.getCapability(TerrariumEarth.worldCap(), null);
        if (earth != null) {
            EntityPlayer player = mc.player;
            player.sendStatusMessage(new TextComponentTranslation("status.earth.panorama.searching"), true);

            Coordinate coordinate = Coordinate.atBlock(player.posX, player.posZ).to(earth.getCrs());
            double longitude = coordinate.x();
            double latitude = coordinate.z();

            Thread thread = new Thread(() -> {
                try {
                    GooglePanorama result = GooglePanorama.lookup(latitude, longitude, 150.0);
                    if (result != null) {
                        Minecraft.getMinecraft().addScheduledTask(() -> this.setPanoramaState(earth, player, result));
                    } else {
                        player.sendStatusMessage(new TextComponentTranslation("status.earth.panorama.none_found"), true);
                    }
                } catch (Exception e) {
                    TerrariumEarth.LOGGER.error("Failed to lookup panorama", e);
                    player.sendStatusMessage(new TextComponentTranslation("status.earth.panorama.error"), true);
                }
            });
            thread.setDaemon(true);
            thread.setName("Panorama Lookup");
            thread.start();
        }
    }

    private void setPanoramaState(EarthWorld earth, EntityPlayer player, GooglePanorama panorama) {
        Coordinate coord = earth.getCrs().coord(panorama.getLongitude(), panorama.getLatitude());
        double blockX = coord.blockX();
        double blockZ = coord.blockZ();

        double deltaX = player.posX - blockX;
        double deltaZ = player.posZ - blockZ;
        if (deltaX * deltaX + deltaZ * deltaZ < PanoramaHandler.IMMERSION_MIN_DISTANCE) {
            player.sendStatusMessage(new TextComponentTranslation("status.earth.panorama.immersed"), true);
            PanoramaHandler.setState(new PanoramaHandler.Immersed(panorama, blockX, player.posY, blockZ));
        } else {
            player.sendStatusMessage(new TextComponentTranslation("status.earth.panorama.found"), true);
            PanoramaHandler.setState(new PanoramaHandler.Located(panorama, blockX, blockZ));
        }
    }
}
