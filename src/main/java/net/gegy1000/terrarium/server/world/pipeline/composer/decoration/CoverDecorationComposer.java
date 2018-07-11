package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.chunk.PseudoRandomMap;
import net.gegy1000.terrarium.server.world.cover.ConstructedCover;
import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class CoverDecorationComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 2492037454623254033L;

    private final Random random;
    private final PseudoRandomMap coverMap;

    private final RegionComponentType<CoverRasterTile> coverComponent;

    private final List<CoverGenerationContext> context;
    private final Map<CoverType<?>, CoverDecorationGenerator<?>> generators;
    private final Set<CoverType<?>> coverTypes = new HashSet<>();

    public CoverDecorationComposer(
            World world,
            RegionComponentType<CoverRasterTile> coverComponent,
            List<ConstructedCover<?>> coverTypes
    ) {
        this.random = new Random(world.getWorldInfo().getSeed() ^ DECORATION_SEED);
        this.coverMap = new PseudoRandomMap(world.getWorldInfo().getSeed(), this.random.nextLong());

        this.coverComponent = coverComponent;

        this.context = coverTypes.stream().map(ConstructedCover::getContext).collect(Collectors.toList());
        this.generators = coverTypes.stream().collect(Collectors.toMap(ConstructedCover::getType, ConstructedCover::createDecorationGenerator));
    }

    @Override
    public void composeDecoration(World world, RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        CoverRasterTile coverRaster = regionHandler.getCachedChunkRaster(this.coverComponent);

        this.coverTypes.clear();
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                this.coverTypes.add(coverRaster.get(localX, localZ));
            }
        }

        this.coverMap.initPosSeed(globalX, globalZ);
        long randomSeed = this.coverMap.next();

        for (CoverGenerationContext context : this.context) {
            context.prepareChunk(regionHandler);
        }

        for (CoverType<?> type : this.coverTypes) {
            CoverDecorationGenerator<?> generator = this.generators.get(type);
            if (generator != null) {
                this.random.setSeed(randomSeed);
                generator.decorate(globalX + 8, globalZ + 8, this.random);
            } else {
                Terrarium.LOGGER.warn("Tried to generate with non-registered cover: {}", type);
            }
        }
    }
}
