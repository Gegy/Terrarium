package net.gegy1000.earth.server.capability;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.event.InitBiomeClassifierEvent;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.EarthInitContext;
import net.gegy1000.earth.server.world.biome.BiomeClassifier;
import net.gegy1000.earth.server.world.biome.StandardBiomeClassifier;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.source.Geocoder;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface EarthWorld extends ICapabilityProvider {
    double EQUATOR_CIRCUMFERENCE = 40075017.0;
    int LOWEST_POINT_METERS = -11100;
    int HIGHEST_POINT_METERS = 8900;

    @Nullable
    static EarthWorld get(World world) {
        return world.getCapability(TerrariumEarth.worldCap(), null);
    }

    Geocoder getGeocoder();

    CoordinateReference getCrs();

    BiomeClassifier getBiomeClassifier();

    @Nullable
    BlockPos estimateSurface(World world, int blockX, int blockZ);

    @Override
    default boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == TerrariumEarth.worldCap();
    }

    @Nullable
    @Override
    default <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == TerrariumEarth.worldCap() ? TerrariumEarth.worldCap().cast(this) : null;
    }

    class None implements EarthWorld {
        @Override
        public Geocoder getGeocoder() {
            return Geocoder.VOID;
        }

        @Override
        public CoordinateReference getCrs() {
            return CoordinateReference.block();
        }

        @Override
        public BiomeClassifier getBiomeClassifier() {
            return BiomeClassifier.DEFAULT;
        }

        @Nullable
        @Override
        public BlockPos estimateSurface(World world, int blockX, int blockZ) {
            return world.getTopSolidOrLiquidBlock(new BlockPos(blockX, 0, blockZ));
        }
    }

    class Impl implements EarthWorld {
        private final CoordinateReference crs;
        private final Geocoder geocoder;
        private final BiomeClassifier classifier;

        public Impl(GenerationSettings settings) {
            this.crs = EarthInitContext.from(settings).lngLatCrs;
            this.geocoder = TerrariumEarth.getPreferredGeocoder();

            StandardBiomeClassifier classifier = new StandardBiomeClassifier();

            InitBiomeClassifierEvent event = new InitBiomeClassifierEvent(settings, classifier);
            MinecraftForge.EVENT_BUS.post(event);

            this.classifier = event.getClassifier();
        }

        @Override
        public Geocoder getGeocoder() {
            return this.geocoder;
        }

        @Override
        public CoordinateReference getCrs() {
            return this.crs;
        }

        @Override
        public BiomeClassifier getBiomeClassifier() {
            return this.classifier;
        }

        @Nullable
        @Override
        public BlockPos estimateSurface(World world, int blockX, int blockZ) {
            TerrariumWorld terrarium = TerrariumWorld.get(world);
            if (terrarium == null) return null;

            ColumnDataCache dataCache = terrarium.getDataCache();

            ShortRaster.Sampler sampler = ShortRaster.sampler(EarthData.TERRAIN_HEIGHT);

            short height = sampler.sample(dataCache, blockX, blockZ);
            return new BlockPos(blockX, height + 1, blockZ);
        }
    }
}
