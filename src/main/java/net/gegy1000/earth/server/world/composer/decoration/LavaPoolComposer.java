package net.gegy1000.earth.server.world.composer.decoration;

import dev.gegy.gengen.api.CubicPos;
import dev.gegy.gengen.api.writer.ChunkPopulationWriter;
import dev.gegy.gengen.util.SpatialRandom;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenLakes;

public class LavaPoolComposer implements DecorationComposer {
    private static final int DEPTH = 64 - 10;
    private static final int CHANCE = 30;

    private static final WorldGenLakes GENERATOR = new WorldGenLakes(Blocks.LAVA);

    private final ShortRaster.Sampler heightSampler = ShortRaster.sampler(EarthData.TERRAIN_HEIGHT);
    private final SpatialRandom random;

    public LavaPoolComposer(World world) {
        this.random = new SpatialRandom(world, 1234);
    }

    @Override
    public void composeDecoration(TerrariumWorld terrarium, CubicPos cubePos, ChunkPopulationWriter writer) {
        ColumnDataCache dataCache = terrarium.getDataCache();

        int surfaceHeight = this.heightSampler.sample(dataCache, cubePos.getMaxX(), cubePos.getMaxZ());
        if (cubePos.getMinY() > surfaceHeight - DEPTH) {
            return;
        }

        World world = writer.getGlobal();

        this.random.setSeed(cubePos.getX(), cubePos.getY(), cubePos.getZ());

        if (this.random.nextInt(CHANCE) == 0) {
            int x = cubePos.getCenterX() + this.random.nextInt(16);
            int y = cubePos.getCenterY() + this.random.nextInt(16);
            int z = cubePos.getCenterZ() + this.random.nextInt(16);
            GENERATOR.generate(world, this.random, new BlockPos(x, y, z));
        }
    }
}
