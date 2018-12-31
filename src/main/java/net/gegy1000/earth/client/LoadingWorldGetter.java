package net.gegy1000.earth.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelInfo;

import java.lang.reflect.Field;

@Environment(EnvType.CLIENT)
public class LoadingWorldGetter {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private static Field levelInfoField;

    static {
        try {
            LoadingWorldGetter.levelInfoField = LoadingWorldGetter.reflectLevelInfoField();
        } catch (Exception e) {
            Terrarium.LOGGER.warn("Failed to reflect level info field", e);
        }
    }

    private static Field reflectLevelInfoField() throws Exception {
        Field levelInfoField = null;
        for (Field field : IntegratedServer.class.getDeclaredFields()) {
            if (field.getType() == LevelInfo.class) {
                field.setAccessible(true);
                levelInfoField = field;
            }
        }
        if (levelInfoField == null) {
            throw new ReflectiveOperationException("Could not find LevelInfo field");
        }
        return levelInfoField;
    }

    public static LevelGeneratorType getLoadingWorldType() {
        LevelGeneratorType generatorType = null;
        if (CLIENT.world != null) {
            generatorType = CLIENT.world.getGeneratorType();
        } else if (CLIENT.getServer() != null) {
            try {
                LevelInfo serverSettings = LoadingWorldGetter.getServerSettings(CLIENT.getServer());
                if (serverSettings != null) {
                    generatorType = serverSettings.getGeneratorType();
                }
            } catch (Exception e) {
                Terrarium.LOGGER.warn("Failed to get IntegratedServer world settings", e);
            }
        }
        return generatorType;
    }

    private static LevelInfo getServerSettings(IntegratedServer server) throws Exception {
        if (LoadingWorldGetter.levelInfoField == null) {
            return null;
        }
        return (LevelInfo) LoadingWorldGetter.levelInfoField.get(server);
    }
}
