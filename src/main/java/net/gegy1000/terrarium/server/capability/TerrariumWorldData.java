package net.gegy1000.terrarium.server.capability;

import com.google.common.collect.Sets;
import net.gegy1000.terrarium.server.map.source.GeocodingSource;
import net.gegy1000.terrarium.server.map.source.glob.GlobSource;
import net.gegy1000.terrarium.server.map.source.height.HeightSource;
import net.gegy1000.terrarium.server.map.source.osm.DetailedOverpassSource;
import net.gegy1000.terrarium.server.map.source.osm.GeneralOverpassSource;
import net.gegy1000.terrarium.server.map.source.osm.OutlineOverpassSource;
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

    DetailedOverpassSource getDetailedOverpassSource();

    GeneralOverpassSource getGeneralOverpassSource();

    OutlineOverpassSource getOutlineOverpassSource();

    GeocodingSource getGeocodingSource();

    Set<TiledSource<?>> getTiledSources();

    class Implementation implements TerrariumWorldData {
        private final EarthGenerationSettings settings;
        private final EarthGenerationHandler generationHandler;

        private final HeightSource heightSource;
        private final GlobSource globSource;
        private final GeocodingSource geocodingSource;

        private final DetailedOverpassSource detailedOverpassSource;
        private final GeneralOverpassSource generalOverpassSource;
        private final OutlineOverpassSource outlineOverpassSource;

        private final Set<TiledSource<?>> tiledSources;

        public Implementation(World world) {
            this.settings = EarthGenerationSettings.deserialize(world.getWorldInfo().getGeneratorOptions());

            this.heightSource = new HeightSource(this.settings);
            this.globSource = new GlobSource(this.settings);
            this.geocodingSource = new GeocodingSource(this.settings);

            this.detailedOverpassSource = new DetailedOverpassSource(this.settings);
            this.generalOverpassSource = new GeneralOverpassSource(this.settings);
            this.outlineOverpassSource = new OutlineOverpassSource(this.settings);

            this.tiledSources = Collections.unmodifiableSet(Sets.newHashSet(
                    this.heightSource, this.globSource,
                    this.detailedOverpassSource, this.generalOverpassSource, this.outlineOverpassSource
            ));

            this.generationHandler = new EarthGenerationHandler(world.getSeed(), this, this.settings, world.getHeight() - 1);

            this.detailedOverpassSource.loadQuery();
            this.generalOverpassSource.loadQuery();
            this.outlineOverpassSource.loadQuery();
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
        public DetailedOverpassSource getDetailedOverpassSource() {
            return this.detailedOverpassSource;
        }

        @Override
        public GeneralOverpassSource getGeneralOverpassSource() {
            return this.generalOverpassSource;
        }

        @Override
        public OutlineOverpassSource getOutlineOverpassSource() {
            return this.outlineOverpassSource;
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
