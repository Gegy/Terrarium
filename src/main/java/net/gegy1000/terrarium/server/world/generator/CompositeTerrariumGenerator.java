package net.gegy1000.terrarium.server.world.generator;

import com.google.common.collect.ImmutableList;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.composer.decoration.CompositeDecorationComposer;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.composer.structure.CompositeStructureComposer;
import net.gegy1000.terrarium.server.world.composer.structure.StructureComposer;
import net.gegy1000.terrarium.server.world.composer.surface.CompositeSurfaceComposer;
import net.gegy1000.terrarium.server.world.composer.surface.SurfaceComposer;

public class CompositeTerrariumGenerator implements TerrariumGenerator {
    private final SurfaceComposer surfaceComposer;
    private final DecorationComposer decorationComposer;
    private final StructureComposer structureComposer;
    private final BiomeComposer biomeComposer;

    private final Coordinate spawnPosition;

    private CompositeTerrariumGenerator(
            SurfaceComposer surfaceComposer,
            DecorationComposer decorationComposer,
            StructureComposer structureComposer,
            BiomeComposer biomeComposer,
            Coordinate spawnPosition
    ) {
        this.surfaceComposer = surfaceComposer;
        this.decorationComposer = decorationComposer;
        this.structureComposer = structureComposer;
        this.biomeComposer = biomeComposer;
        this.spawnPosition = spawnPosition;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public SurfaceComposer getSurfaceComposer() {
        return this.surfaceComposer;
    }

    @Override
    public DecorationComposer getDecorationComposer() {
        return this.decorationComposer;
    }

    @Override
    public StructureComposer getStructureComposer() {
        return this.structureComposer;
    }

    @Override
    public BiomeComposer getBiomeComposer() {
        return this.biomeComposer;
    }

    @Override
    public Coordinate getSpawnPosition() {
        return this.spawnPosition;
    }

    public static class Builder {
        private final ImmutableList.Builder<SurfaceComposer> surfaceComposers = new ImmutableList.Builder<>();
        private final ImmutableList.Builder<StructureComposer> structureComposers = new ImmutableList.Builder<>();
        private final ImmutableList.Builder<DecorationComposer> decorationComposers = new ImmutableList.Builder<>();
        private BiomeComposer biomeComposer = BiomeComposer.Default.INSTANCE;

        private Coordinate spawnPosition;

        private Builder() {
        }

        public Builder addSurfaceComposer(SurfaceComposer composer) {
            this.surfaceComposers.add(composer);
            return this;
        }

        public Builder addStructureComposer(StructureComposer composer) {
            this.structureComposers.add(composer);
            return this;
        }

        public Builder addDecorationComposer(DecorationComposer composer) {
            this.decorationComposers.add(composer);
            return this;
        }

        public Builder setBiomeComposer(BiomeComposer composer) {
            this.biomeComposer = composer;
            return this;
        }

        public Builder setSpawnPosition(Coordinate coordinate) {
            this.spawnPosition = coordinate;
            return this;
        }

        public CompositeTerrariumGenerator build() {
            return new CompositeTerrariumGenerator(
                    CompositeSurfaceComposer.of(this.surfaceComposers.build()),
                    CompositeDecorationComposer.of(this.decorationComposers.build()),
                    CompositeStructureComposer.of(this.structureComposers.build()),
                    this.biomeComposer,
                    this.spawnPosition
            );
        }
    }
}
