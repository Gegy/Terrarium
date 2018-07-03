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
