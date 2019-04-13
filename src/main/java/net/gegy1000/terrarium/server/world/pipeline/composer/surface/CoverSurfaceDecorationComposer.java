package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.cubicglue.util.PseudoRandomMap;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.cover.ConstructedCover;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverSurfaceGenerator;
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

public class CoverSurfaceDecorationComposer implements SurfaceComposer {
    private static final long SEED = 6035435416693430887L;

    private final Random random;
    private final PseudoRandomMap coverMap;

    private final RegionComponentType<CoverRaster> coverComponent;

    private final List<CoverGenerationContext> context;
    private final Map<CoverType<?>, CoverSurfaceGenerator<?>> generators;
    private final Set<CoverType<?>> localCoverTypes = new HashSet<>();

    public CoverSurfaceDecorationComposer(
            World world,
            RegionComponentType<CoverRaster> coverComponent,
            List<ConstructedCover<?>> coverTypes
    ) {
        this.random = new Random(world.getWorldInfo().getSeed() ^ SEED);
        this.coverMap = new PseudoRandomMap(world.getWorldInfo().getSeed(), this.random.nextLong());

        this.coverComponent = coverComponent;

        this.context = coverTypes.stream().map(ConstructedCover::getContext).collect(Collectors.toList());
        this.generators = coverTypes.stream().collect(Collectors.toMap(ConstructedCover::getType, ConstructedCover::createSurfaceGenerator));
    }

    @Override
    public void composeSurface(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPrimeWriter writer) {
        int globalX = pos.getMinX();
        int globalY = pos.getMinY();
        int globalZ = pos.getMinZ();

        CoverRaster coverRaster = regionHandler.getCachedChunkRaster(this.coverComponent);

        for (CoverGenerationContext context : this.context) {
            context.prepareChunk(regionHandler);
        }

        this.localCoverTypes.clear();
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                this.localCoverTypes.add(coverRaster.get(localX, localZ));
            }
        }

        this.coverMap.initPosSeed(globalX, globalY, globalZ);
        long randomSeed = this.coverMap.next();

        for (CoverType type : this.localCoverTypes) {
            CoverSurfaceGenerator<?> coverGenerator = this.generators.get(type);
            if (coverGenerator != null) {
                this.random.setSeed(randomSeed);
                this.random.setSeed(this.random.nextLong());
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
