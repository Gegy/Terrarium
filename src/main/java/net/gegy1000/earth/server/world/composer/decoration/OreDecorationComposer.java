package net.gegy1000.earth.server.world.composer.decoration;

import dev.gegy.gengen.api.CubicPos;
import dev.gegy.gengen.api.writer.ChunkPopulationWriter;
import dev.gegy.gengen.core.GenGen;
import dev.gegy.gengen.util.SpatialRandom;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.ores.OreConfig;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class OreDecorationComposer implements DecorationComposer {
    private final ShortRaster.Sampler heightSampler = ShortRaster.sampler(EarthData.TERRAIN_HEIGHT);
    private final SpatialRandom random;

    private final Collection<OreConfig> ores = new ArrayList<>();

    public OreDecorationComposer(World world) {
        this.random = new SpatialRandom(world, 1);
    }

    public OreDecorationComposer add(OreConfig... ores) {
        Collections.addAll(this.ores, ores);
        return this;
    }

    @Override
    public void composeDecoration(TerrariumWorld terrarium, CubicPos cubePos, ChunkPopulationWriter writer) {
        ColumnDataCache dataCache = terrarium.getDataCache();
        int x = cubePos.getMaxX();
        int z = cubePos.getMaxZ();

        int surfaceHeight = this.heightSampler.sample(dataCache, x, z);

        // quick exit if this chunk is above the terrain surface
        if (cubePos.getMinY() > surfaceHeight + 16) {
            return;
        }

        World world = writer.getGlobal();
        boolean cubic = GenGen.isCubic(world);

        this.random.setSeed(cubePos.getX(), cubePos.getY(), cubePos.getZ());

        if (!cubic) {
            // when cubic chunks is not enabled, all ores need to still have space within the world
            surfaceHeight = Math.max(surfaceHeight, 64);
        }

        for (OreConfig ore : this.ores) {
            if (!ore.getSelector().shouldGenerateAt(dataCache, x, z)) continue;

            ore.getDistribution().forChunk(cubePos, surfaceHeight, this.random).forEach(pos -> {
                if (!cubic && pos.getY() <= 1) return;
                ore.getGenerator().generate(world, this.random, pos);
            });
        }
    }
}
