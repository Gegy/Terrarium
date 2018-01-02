package net.gegy1000.terrarium.client;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.render.LoadingScreenOverlay;
import net.gegy1000.terrarium.server.ServerProxy;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class ClientProxy extends ServerProxy {
    private static Field selectedWorldType;

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

        try {
            ClientProxy.selectedWorldType = reflectSelectedWorldType();
        } catch (ReflectiveOperationException e) {
            Terrarium.LOGGER.warn("Failed to reflect selected world type", e);
        }

        LoadingScreenOverlay.onPostInit();
        LoadingWorldGetter.onPostInit();
    }

    private static Field reflectSelectedWorldType() throws ReflectiveOperationException {
        for (Field field : GuiCreateWorld.class.getDeclaredFields()) {
            if (field.getType() == int.class) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new ReflectiveOperationException("Could not find selected world type field");
    }

    public static int getSelectedWorldType(GuiCreateWorld gui) {
        if (ClientProxy.selectedWorldType == null) {
            return 0;
        }
        try {
            return (int) ClientProxy.selectedWorldType.get(gui);
        } catch (IllegalAccessException e) {
            return 0;
        }
    }
}
