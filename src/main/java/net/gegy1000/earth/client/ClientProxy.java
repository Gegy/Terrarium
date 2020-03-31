package net.gegy1000.earth.client;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.client.gui.EarthLocateGui;
import net.gegy1000.earth.client.gui.EarthPreloadGui;
import net.gegy1000.earth.client.gui.EarthPreloadProgressGui;
import net.gegy1000.earth.client.render.LoadingScreenOverlay;
import net.gegy1000.earth.client.render.PanoramaHandler;
import net.gegy1000.earth.client.render.PanoramaLookupHandler;
import net.gegy1000.earth.server.ServerProxy;
import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.message.EarthOpenMapMessage;
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
    public void onPreInit() {
    }

    @Override
    public void onInit() {
    }

    @Override
    public void onPostInit() {
        LoadingScreenOverlay.onPostInit();
        LoadingWorldGetter.onPostInit();
    }

    @Override
    public void updateDownload(int id, long count, long total) {
        Minecraft client = Minecraft.getMinecraft();
        if (count == 0 && client.currentScreen == null) {
            client.displayGuiScreen(new EarthPreloadProgressGui(id));
        }

        if (client.currentScreen instanceof EarthPreloadProgressGui) {
            ((EarthPreloadProgressGui) client.currentScreen).update(id, count, total);
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
            double longitude = coordinate.getX();
            double latitude = coordinate.getZ();

            Thread thread = new Thread(() -> {
                try {
                    PanoramaLookupHandler.Result result = PanoramaLookupHandler.queryPanorama(latitude, longitude);
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

    private void setPanoramaState(EarthWorld earth, EntityPlayer player, PanoramaLookupHandler.Result result) {
        Coordinate coord = new Coordinate(earth.getCrs(), result.getLongitude(), result.getLatitude());
        double blockX = coord.getBlockX();
        double blockZ = coord.getBlockZ();

        double deltaX = player.posX - blockX;
        double deltaZ = player.posZ - blockZ;
        if (deltaX * deltaX + deltaZ * deltaZ < PanoramaHandler.IMMERSION_MIN_DISTANCE) {
            player.sendStatusMessage(new TextComponentTranslation("status.earth.panorama.immersed"), true);
            PanoramaHandler.setState(new PanoramaHandler.Immersed(result.getId(), blockX, player.posY, blockZ));
        } else {
            player.sendStatusMessage(new TextComponentTranslation("status.earth.panorama.found"), true);
            PanoramaHandler.setState(new PanoramaHandler.Located(result.getId(), coord));
        }
    }
}
