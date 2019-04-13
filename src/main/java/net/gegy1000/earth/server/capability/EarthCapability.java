package net.gegy1000.earth.server.capability;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.pipeline.source.Geocoder;
import net.gegy1000.terrarium.server.world.region.GenerationRegion;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface EarthCapability extends ICapabilityProvider {
    Geocoder getGeocoder();

    CoordinateState getGeoCoordinate();

    double getLatitude(double x, double z);

    double getLongitude(double x, double z);

    double getX(double latitude, double longitude);

    double getZ(double latitude, double longitude);

    @Nullable
    BlockPos estimateSurface(World world, int blockX, int blockZ);

    @Override
    default boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == TerrariumEarth.earthCap;
    }

    @Nullable
    @Override
    default <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == TerrariumEarth.earthCap) {
            return TerrariumEarth.earthCap.cast(this);
        }
        return null;
    }

    class None implements EarthCapability {
        @Override
        public Geocoder getGeocoder() {
            return Geocoder.VOID;
        }

        @Override
        public CoordinateState getGeoCoordinate() {
            return CoordinateState.BLOCK;
        }

        @Override
        public double getLatitude(double x, double z) {
            return CoordinateState.BLOCK.getX(x, z);
        }

        @Override
        public double getLongitude(double x, double z) {
            return CoordinateState.BLOCK.getZ(x, z);
        }

        @Override
        public double getX(double latitude, double longitude) {
            return CoordinateState.BLOCK.getBlockX(latitude, longitude);
        }

        @Override
        public double getZ(double latitude, double longitude) {
            return CoordinateState.BLOCK.getBlockZ(latitude, longitude);
        }

        @Nullable
        @Override
        public BlockPos estimateSurface(World world, int blockX, int blockZ) {
            return world.getTopSolidOrLiquidBlock(new BlockPos(blockX, 0, blockZ));
        }
    }

    class Impl implements EarthCapability {
        private final CoordinateState geoCoordinate;
        private final Geocoder geocoder;

        public Impl(CoordinateState geoCoordinate) {
            this.geoCoordinate = geoCoordinate;
            this.geocoder = TerrariumEarth.getPreferredGeocoder();
        }

        @Override
        public Geocoder getGeocoder() {
            return this.geocoder;
        }

        @Override
        public CoordinateState getGeoCoordinate() {
            return this.geoCoordinate;
        }

        @Override
        public double getLatitude(double x, double z) {
            return this.geoCoordinate.getX(x, z);
        }

        @Override
        public double getLongitude(double x, double z) {
            return this.geoCoordinate.getZ(x, z);
        }

        @Override
        public double getX(double latitude, double longitude) {
            return this.geoCoordinate.getBlockX(latitude, longitude);
        }

        @Override
        public double getZ(double latitude, double longitude) {
            return this.geoCoordinate.getBlockZ(latitude, longitude);
        }

        @Nullable
        @Override
        public BlockPos estimateSurface(World world, int blockX, int blockZ) {
            TerrariumWorldData worldData = TerrariumWorldData.get(world);
            if (worldData == null) {
                return null;
            }

            RegionGenerationHandler regionHandler = worldData.getRegionHandler();
            GenerationRegion region = regionHandler.get(blockX, blockZ);
            ShortRaster heightRaster = region.getData().get(RegionComponentType.HEIGHT);
            if (heightRaster == null) {
                return null;
            }

            int height = heightRaster.getShort(blockX - region.getMinX(), blockZ - region.getMinZ()) + 1;
            return new BlockPos(blockX, height, blockZ);
        }
    }
}
