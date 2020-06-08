package net.gegy1000.earth.server.world.compatibility;

import io.github.opencubicchunks.cubicchunks.api.util.Box;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.opencubicchunks.cubicchunks.core.world.cube.Cube;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.core.GenGen;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.function.Consumer;

public final class ColumnCompatibility {
    private static final ShortRaster.Sampler HEIGHT_SAMPLER = ShortRaster.sampler(EarthData.TERRAIN_HEIGHT);
    private static final int COMPAT_SURFACE_Y = 74;

    private final World world;
    private final boolean cubic;
    private final ColumnCompatibilityWorld compatibilityWorld;

    private boolean recursing;

    public ColumnCompatibility(World world) {
        this.world = world;
        this.cubic = GenGen.isCubic(world);
        this.compatibilityWorld = new ColumnCompatibilityWorld(world);
    }

    public void generateInColumn(TerrariumWorld terrarium, CubicPos pos, Consumer<ColumnCompatibilityWorld> generator) {
        ChunkPos columnPos = new ChunkPos(pos.getX(), pos.getZ());

        int minY = this.getMinCompatibilityY(terrarium, columnPos);
        int minCubeY = minY >> 4;
        int maxCubeY = minCubeY + 15;

        int cubeY = pos.getY();
        if (cubeY < minCubeY || cubeY > maxCubeY) return;

        if (this.prepareColumn(columnPos, minCubeY, maxCubeY)) {
            try {
                this.compatibilityWorld.setupAt(columnPos, minCubeY << 4);
                generator.accept(this.compatibilityWorld);
            } finally {
                this.compatibilityWorld.close();
            }
        }
    }

    private boolean prepareColumn(ChunkPos columnPos, int minCubeY, int maxCubeY) {
        if (this.recursing) return false;

        try {
            this.recursing = true;

            // in a vanilla world, we know the full column should be already populated
            if (this.cubic) {
                this.prepareCubes(columnPos, minCubeY, maxCubeY);
            }

            return true;
        } finally {
            this.recursing = false;
        }
    }

    private void prepareCubes(ChunkPos columnPos, int minCubeY, int maxCubeY) {
        ICubicWorld cubicWorld = (ICubicWorld) this.world;

        if (cubicWorld.getCubeCache() instanceof CubeProviderServer) {
            CubeProviderServer cache = (CubeProviderServer) cubicWorld.getCubeCache();
            ICubeGenerator generator = cache.getCubeGenerator();

            for (int cubeY = minCubeY; cubeY <= maxCubeY; cubeY++) {
                Cube cube = cache.getCube(columnPos.x, cubeY, columnPos.z);
                this.populateCube(cache, generator, cube);
            }
        }
    }

    private void populateCube(CubeProviderServer cache, ICubeGenerator generator, Cube cube) {
        Box requirements = generator.getPopulationPregenerationRequirements(cube);
        requirements.forEachPoint((x, y, z) -> {
            cache.getCube(cube.getX() + x, cube.getY() + y, cube.getZ() + z);
        });

        if (!cube.isPopulated()) {
            generator.populate(cube);
            cube.setPopulated(true);
        }
    }

    private int getMinCompatibilityY(TerrariumWorld terrarium, ChunkPos columnPos) {
        int x = columnPos.getXStart() + 8;
        int z = columnPos.getZStart() + 8;

        int surfaceY = HEIGHT_SAMPLER.sample(terrarium.getDataCache(), x, z);
        if (!this.cubic) {
            // when cubic chunks is not enabled, all generation still needs space within the world
            surfaceY = Math.max(surfaceY, COMPAT_SURFACE_Y);
        }

        return surfaceY - COMPAT_SURFACE_Y;
    }
}
