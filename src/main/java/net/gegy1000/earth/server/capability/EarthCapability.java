package net.gegy1000.earth.server.capability;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.pipeline.source.GoogleGeocoder;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.source.Geocoder;
import net.minecraft.util.EnumFacing;
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

    class Impl implements EarthCapability {
        private final CoordinateState geoCoordinate;
        private final Geocoder geocoder;

        public Impl(CoordinateState geoCoordinate) {
            this.geoCoordinate = geoCoordinate;
            this.geocoder = new GoogleGeocoder(geoCoordinate);
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

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == TerrariumEarth.earthCap;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == TerrariumEarth.earthCap) {
                return TerrariumEarth.earthCap.cast(this);
            }
            return null;
        }
    }
}
