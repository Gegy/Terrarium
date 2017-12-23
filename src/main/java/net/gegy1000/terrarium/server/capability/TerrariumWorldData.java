package net.gegy1000.terrarium.server.capability;

import com.google.common.collect.Sets;
import net.gegy1000.terrarium.server.map.source.GeocodingSource;
import net.gegy1000.terrarium.server.map.source.glob.GlobSource;
import net.gegy1000.terrarium.server.map.source.height.HeightSource;
import net.gegy1000.terrarium.server.map.source.osm.OverpassSource;
import net.gegy1000.terrarium.server.map.source.tiled.TiledSource;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.gegy1000.terrarium.server.world.generator.EarthGenerationHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.Collections;
import java.util.Set;

public interface TerrariumWorldData extends ICapabilityProvider {
    EarthGenerationHandler getGenerationHandler();

    HeightSource getHeightSource();

    GlobSource getGlobSource();

    OverpassSource getOverpassSource();

    GeocodingSource getGeocodingSource();

    Set<TiledSource<?>> getTiledSources();

    class Implementation implements TerrariumWorldData {
        private final EarthGenerationSettings settings;
        private final EarthGenerationHandler generationHandler;

        private final HeightSource heightSource;
        private final GlobSource globSource;
        private final OverpassSource overpassSource;
        private final GeocodingSource geocodingSource;

        private final Set<TiledSource<?>> tiledSources;

        public Implementation(World world) {
            this.settings = EarthGenerationSettings.deserialize(world.getWorldInfo().getGeneratorOptions());
            this.generationHandler = new EarthGenerationHandler(this, this.settings, world.getHeight() - 1);

            this.heightSource = new HeightSource(this.settings);
            this.globSource = new GlobSource(this.settings);
            this.overpassSource = new OverpassSource(this.settings);
            this.geocodingSource = new GeocodingSource(this.settings);

            this.tiledSources = Collections.unmodifiableSet(Sets.newHashSet(this.heightSource, this.globSource, this.overpassSource));

            this.overpassSource.loadQuery();
        }

        @Override
        public EarthGenerationHandler getGenerationHandler() {
            return this.generationHandler;
        }

        @Override
        public HeightSource getHeightSource() {
            return this.heightSource;
        }

        @Override
        public GlobSource getGlobSource() {
            return this.globSource;
        }

        @Override
        public OverpassSource getOverpassSource() {
            return this.overpassSource;
        }

        @Override
        public GeocodingSource getGeocodingSource() {
            return this.geocodingSource;
        }

        @Override
        public Set<TiledSource<?>> getTiledSources() {
            return this.tiledSources;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
            return capability == TerrariumCapabilities.worldDataCapability;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            if (this.hasCapability(capability, facing)) {
                return TerrariumCapabilities.worldDataCapability.cast(this);
            }
            return null;
        }
    }
}
