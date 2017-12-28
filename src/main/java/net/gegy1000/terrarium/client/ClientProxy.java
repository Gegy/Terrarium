package net.gegy1000.terrarium.client;

import net.gegy1000.terrarium.client.render.LoadingScreenOverlay;
import net.gegy1000.terrarium.server.ServerProxy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends ServerProxy {
    @Override
    public void onPreInit() {
        super.onPreInit();
    }

    @Override
    public void onInit() {
        super.onInit();
    }

    @Override
    public void onPostInit() {
        super.onPostInit();

        LoadingScreenOverlay.onPostInit();
        LoadingWorldGetter.onPostInit();
    }
}
