package net.gegy1000.terrarium.server.map.system.component;

import net.minecraft.util.ResourceLocation;

public interface RegionComponentType<T> {
    T createDefaultData(int width, int height);

    ResourceLocation getIdentifier();
}
