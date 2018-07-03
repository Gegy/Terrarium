package net.gegy1000.earth.client;

import net.gegy1000.terrarium.Terrarium;
import net.minecraft.client.Minecraft;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class LoadingWorldGetter {
    private static final Minecraft MC = Minecraft.getMinecraft();

    private static Field worldSettingsField;

    public static void onPostInit() {
        try {
            LoadingWorldGetter.worldSettingsField = LoadingWorldGetter.reflectWorldSettingsField();
        } catch (Exception e) {
            Terrarium.LOGGER.warn("Failed to reflect world settings field", e);
        }
    }

    private static Field reflectWorldSettingsField() throws Exception {
        Field worldSettingsField = null;
        for (Field field : IntegratedServer.class.getDeclaredFields()) {
            if (field.getType() == WorldSettings.class) {
                field.setAccessible(true);
                worldSettingsField = field;
            }
        }
        if (worldSettingsField == null) {
            throw new ReflectiveOperationException("Could not find WorldSettings field");
        }
        return worldSettingsField;
    }

    public static WorldType getLoadingWorldType() {
        WorldType worldType = null;
        if (MC.world != null) {
            worldType = MC.world.getWorldType();
        } else if (MC.getIntegratedServer() != null) {
            try {
                WorldSettings serverSettings = LoadingWorldGetter.getServerSettings(MC.getIntegratedServer());
                if (serverSettings != null) {
                    worldType = serverSettings.getTerrainType();
                }
            } catch (Exception e) {
                Terrarium.LOGGER.warn("Failed to get IntegratedServer world settings", e);
            }
        }
        return worldType;
    }

    private static WorldSettings getServerSettings(IntegratedServer server) throws Exception {
        if (LoadingWorldGetter.worldSettingsField == null) {
            return null;
        }
        return (WorldSettings) LoadingWorldGetter.worldSettingsField.get(server);
    }
}
