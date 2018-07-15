package net.gegy1000.earth.client;

import net.gegy1000.earth.client.gui.EarthLocateGui;
import net.gegy1000.earth.client.gui.EarthTeleportGui;
import net.gegy1000.earth.client.render.LoadingScreenOverlay;
import net.gegy1000.earth.server.ServerProxy;
import net.gegy1000.earth.server.message.EarthMapGuiMessage;
import net.minecraft.client.Minecraft;
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
}
