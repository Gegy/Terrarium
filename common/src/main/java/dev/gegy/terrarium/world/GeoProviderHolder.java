package dev.gegy.terrarium.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.RandomState;
import org.jetbrains.annotations.Nullable;

public interface GeoProviderHolder {
    static void inject(final RandomState randomState, final GeoProvider provider) {
        ((GeoProviderHolder) (Object) randomState).terrarium$setGeoProvider(provider);
        ((GeoProviderHolder) (Object) randomState.sampler()).terrarium$setGeoProvider(provider);
    }

    @Nullable
    static GeoProvider get(final ServerLevel level) {
        return get(level.getChunkSource().randomState());
    }

    @Nullable
    static GeoProvider get(final RandomState randomState) {
        return ((GeoProviderHolder) (Object) randomState).terrarium$getGeoProvider();
    }

    @Nullable
    static GeoProvider get(final Climate.Sampler sampler) {
        return ((GeoProviderHolder) (Object) sampler).terrarium$getGeoProvider();
    }

    void terrarium$setGeoProvider(GeoProvider provider);

    @Nullable
    GeoProvider terrarium$getGeoProvider();
}
