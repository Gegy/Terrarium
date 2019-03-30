package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.cubicglue.util.PseudoRandomMap;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.cover.ConstructedCover;
import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.CoverRaster;
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

    private final RegionComponentType<CoverRaster> coverComponent;

    private final List<CoverGenerationContext> context;
    private final Map<CoverType<?>, CoverDecorationGenerator<?>> generators;
    private final Set<CoverType<?>> coverTypes = new HashSet<>();

    public CoverDecorationComposer(
            World world,
            RegionComponentType<CoverRaster> coverComponent,
            List<ConstructedCover<?>> coverTypes
    ) {
        this.random = new Random(world.getWorldInfo().getSeed() ^ DECORATION_SEED);
        this.coverMap = new PseudoRandomMap(world.getWorldInfo().getSeed(), this.random.nextLong());

        this.coverComponent = coverComponent;

        this.context = coverTypes.stream().map(ConstructedCover::getContext).collect(Collectors.toList());
        this.generators = coverTypes.stream().collect(Collectors.toMap(ConstructedCover::getType, ConstructedCover::createDecorationGenerator));
    }

    @Override
    public void composeDecoration(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPopulationWriter writer) {
        int globalX = pos.getMinX();
        int globalY = pos.getMinY();
        int globalZ = pos.getMinZ();

        CoverRaster coverRaster = regionHandler.getCachedChunkRaster(this.coverComponent);

        this.coverTypes.clear();
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                this.coverTypes.add(coverRaster.get(localX, localZ));
            }
        }

        this.coverMap.initPosSeed(globalX, globalY, globalZ);
        long randomSeed = this.coverMap.next();

        for (CoverGenerationContext context : this.context) {
            context.prepareChunk(regionHandler);
        }

        for (CoverType<?> type : this.coverTypes) {
            CoverDecorationGenerator<?> coverGenerator = this.generators.get(type);
            if (coverGenerator != null) {
                this.random.setSeed(randomSeed);
                coverGenerator.decorate(pos, writer, this.random);
            } else {
                Terrarium.LOGGER.warn("Tried to generate with non-registered cover: {}", type);
            }
        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[] { this.coverComponent };
    }
}
