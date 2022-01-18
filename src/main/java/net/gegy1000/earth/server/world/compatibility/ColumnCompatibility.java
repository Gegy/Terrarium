package net.gegy1000.earth.server.world.compatibility;

import dev.gegy.gengen.api.CubicPos;
import dev.gegy.gengen.core.GenGen;
import io.github.opencubicchunks.cubicchunks.api.util.Box;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.opencubicchunks.cubicchunks.core.world.cube.Cube;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.compatibility.capability.CcColumnCompatibilityMetadata;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

public final class ColumnCompatibility {
    private static final ShortRaster.Sampler HEIGHT_SAMPLER = ShortRaster.sampler(EarthData.TERRAIN_HEIGHT);
    private static final int COMPAT_SURFACE_Y = 74;

    private final World world;
    private final boolean cubic;
    private final ColumnCompatibilityWorld compatibilityWorld;

    public ColumnCompatibility(WorldServer world) {
        this.world = world;
        this.cubic = GenGen.isCubic(world);
        this.compatibilityWorld = ColumnCompatibilityWorld.create(world);
    }

    public void generateInColumn(TerrariumWorld terrarium, CubicPos pos, ColumnGenerator generator) {
        ChunkPos columnPos = new ChunkPos(pos.getX(), pos.getZ());

        GenerationRange range = this.getCompatibilityRange(terrarium, columnPos);
        if (!range.contains(pos.getY())) {
            return;
        }

        if (this.cubic) {
            this.generateInCubicChunksColumn(pos, range, generator);
        } else {
            this.generateInVanillaColumn(pos, range, generator);
        }
    }

    private void generateInVanillaColumn(CubicPos pos, GenerationRange range, ColumnGenerator generator) {
        if (pos.getY() == range.minCubeY) {
            ChunkPos columnPos = new ChunkPos(pos.getX(), pos.getZ());
            try (ColumnCompatibilityWorld world = this.compatibilityWorld.setupAt(columnPos, range.minY())) {
                generator.generate(world, columnPos);
            }
        }
    }

    private void generateInCubicChunksColumn(CubicPos pos, GenerationRange range, ColumnGenerator generator) {
        Chunk chunk = this.world.getChunk(pos.getX(), pos.getZ());

        CcColumnCompatibilityMetadata metadata = CcColumnCompatibilityMetadata.get(chunk);
        if (metadata != null && metadata.tryRunGenerator()) {
            ChunkPos columnPos = new ChunkPos(pos.getX(), pos.getZ());
            Cubic.prepareCubesInColumn(this.world, columnPos, range);

            try (ColumnCompatibilityWorld world = this.compatibilityWorld.setupAt(columnPos, range.minY())) {
                generator.generate(world, columnPos);
            }
        }
    }

    private GenerationRange getCompatibilityRange(TerrariumWorld terrarium, ChunkPos columnPos) {
        int x = columnPos.getXStart() + 8;
        int z = columnPos.getZStart() + 8;

        int surfaceY = HEIGHT_SAMPLER.sample(terrarium.getDataCache(), x, z);
        int minY = surfaceY - COMPAT_SURFACE_Y;

        if (!this.cubic) {
            // when cubic chunks is not enabled, all generation still needs space within the world
            minY = Math.max(surfaceY, 0);
        }

        int minCubeY = minY >> 4;
        int maxCubeY = minCubeY + 15;
        return new GenerationRange(minCubeY, maxCubeY);
    }

    static final class GenerationRange {
        final int minCubeY;
        final int maxCubeY;

        GenerationRange(int minCubeY, int maxCubeY) {
            this.minCubeY = minCubeY;
            this.maxCubeY = maxCubeY;
        }

        public boolean contains(int y) {
            return y >= this.minCubeY && y <= this.maxCubeY;
        }

        public int minY() {
            return this.minCubeY << 4;
        }
    }

    static class Cubic {
        static void prepareCubesInColumn(World world, ChunkPos columnPos, GenerationRange range) {
            ICubicWorld cubicWorld = (ICubicWorld) world;

            if (cubicWorld.getCubeCache() instanceof CubeProviderServer) {
                CubeProviderServer cache = (CubeProviderServer) cubicWorld.getCubeCache();
                ICubeGenerator generator = cache.getCubeGenerator();

                for (int cubeY = range.minCubeY; cubeY <= range.maxCubeY; cubeY++) {
                    Cube cube = cache.getCube(columnPos.x, cubeY, columnPos.z);
                    populateCube(cache, generator, cube);
                }
            }
        }

        static void populateCube(CubeProviderServer cache, ICubeGenerator generator, Cube cube) {
            Box requirements = generator.getPopulationPregenerationRequirements(cube);
            requirements.forEachPoint((x, y, z) -> {
                cache.getCube(cube.getX() + x, cube.getY() + y, cube.getZ() + z);
            });

            if (!cube.isPopulated()) {
                generator.populate(cube);
                cube.setPopulated(true);
            }
        }
    }

    public interface ColumnGenerator {
        void generate(ColumnCompatibilityWorld world, ChunkPos columnPos);
    }
}
