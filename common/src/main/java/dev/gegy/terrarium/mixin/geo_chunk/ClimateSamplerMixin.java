package dev.gegy.terrarium.mixin.geo_chunk;

import dev.gegy.terrarium.world.GeoProvider;
import dev.gegy.terrarium.world.GeoProviderHolder;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Climate.Sampler.class)
public class ClimateSamplerMixin implements GeoProviderHolder {
    @Unique
    @Nullable
    private GeoProvider terrarium$geoProvider;

    @Override
    public void terrarium$setGeoProvider(final GeoProvider provider) {
        terrarium$geoProvider = provider;
    }

    @Override
    @Nullable
    public GeoProvider terrarium$getGeoProvider() {
        return terrarium$geoProvider;
    }
}
