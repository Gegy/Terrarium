package net.gegy1000.earth.client;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.client.gui.EarthLocateGui;
import net.gegy1000.earth.client.gui.EarthTeleportGui;
import net.gegy1000.earth.client.render.LoadingScreenOverlay;
import net.gegy1000.earth.client.render.PanoramaHandler;
import net.gegy1000.earth.client.render.PanoramaLookupHandler;
import net.gegy1000.earth.server.ServerProxy;
import net.gegy1000.earth.server.capability.EarthCapability;
import net.gegy1000.earth.server.message.EarthMapGuiMessage;
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
    public void openMapGui(EarthMapGuiMessage.Type type, double latitude, double longitude) {
        Minecraft mc = Minecraft.getMinecraft();
        switch (type) {
            case LOCATE:
                mc.displayGuiScreen(new EarthLocateGui(latitude, longitude));
                break;
            case TELEPORT:
                mc.displayGuiScreen(new EarthTeleportGui(latitude, longitude));
        }
    }

    @Override
    public void displayPanorama() {
        Minecraft mc = Minecraft.getMinecraft();

        World world = mc.world;
        EarthCapability earthData = world.getCapability(TerrariumEarth.earthCap, null);
        if (earthData != null) {
            EntityPlayer player = mc.player;
            player.sendStatusMessage(new TextComponentTranslation("status.earth.panorama.searching"), true);

            double latitude = earthData.getLatitude(player.posX, player.posZ);
            double longitude = earthData.getLongitude(player.posX, player.posZ);

            Thread thread = new Thread(() -> {
                try {
                    PanoramaLookupHandler.Result result = PanoramaLookupHandler.queryPanorama(latitude, longitude);
                    if (result != null) {
                        Minecraft.getMinecraft().addScheduledTask(() -> this.setPanoramaState(earthData, player, result));
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

    private void setPanoramaState(EarthCapability earthData, EntityPlayer player, PanoramaLookupHandler.Result result) {
        double blockX = earthData.getX(result.getLatitude(), result.getLongitude());
        double blockZ = earthData.getZ(result.getLatitude(), result.getLongitude());
        double deltaX = player.posX - blockX;
        double deltaZ = player.posZ - blockZ;
        if (deltaX * deltaX + deltaZ * deltaZ < PanoramaHandler.IMMERSION_MIN_DISTANCE) {
            player.sendStatusMessage(new TextComponentTranslation("status.earth.panorama.immersed"), true);
            PanoramaHandler.setState(new PanoramaHandler.Immersed(result.getId(), blockX, player.posY, blockZ));
        } else {
            player.sendStatusMessage(new TextComponentTranslation("status.earth.panorama.found"), true);
            PanoramaHandler.setState(new PanoramaHandler.Located(result.getId(), result.getLatitude(), result.getLongitude()));
        }
    }
}
