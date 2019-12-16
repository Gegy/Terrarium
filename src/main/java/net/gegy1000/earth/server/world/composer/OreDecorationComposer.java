package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.world.ores.OreConfig;
import net.gegy1000.earth.server.world.ores.OreDistribution;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.core.GenGen;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class OreDecorationComposer implements DecorationComposer {
    private final ShortRaster.Sampler heightSampler;
    private final SpatialRandom random;

    private final Collection<OreConfig> ores = new ArrayList<>();

    public OreDecorationComposer(World world, DataKey<ShortRaster> heightKey) {
        this.heightSampler = ShortRaster.sampler(heightKey);
        this.random = new SpatialRandom(world, 1);
    }

    public OreDecorationComposer add(OreConfig... ores) {
        Collections.addAll(this.ores, ores);
        return this;
    }

    @Override
    public void composeDecoration(ColumnDataCache dataCache, CubicPos cubePos, ChunkPopulationWriter writer) {
        this.random.setSeed(cubePos.getX(), cubePos.getY(), cubePos.getZ());

        int surfaceHeight = this.heightSampler.sample(dataCache, cubePos.getMaxX(), cubePos.getMaxZ());

        World world = writer.getGlobal();
        boolean cubic = GenGen.isCubic(world);

        for (OreConfig ore : this.ores) {
            WorldGenMinable generator = ore.getGenerator();
            OreDistribution distribution = ore.getDistribution();
            distribution.forChunk(cubePos, surfaceHeight, this.random).forEach(pos -> {
                if (!cubic && pos.getY() <= 1) return;
                generator.generate(world, this.random, pos);
            });
        }
    }
}
