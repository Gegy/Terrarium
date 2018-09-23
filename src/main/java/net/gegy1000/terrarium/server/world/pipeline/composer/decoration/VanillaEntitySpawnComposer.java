package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.cubicglue.CubicGlue;
import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.cubicglue.util.PseudoRandomMap;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;

import java.util.Random;

public class VanillaEntitySpawnComposer implements DecorationComposer {
    private static final long SPAWN_SEED = 24933181514746343L;

    private final PseudoRandomMap randomMap;
    private final Random random = new Random(0);

    public VanillaEntitySpawnComposer(World world) {
        this.randomMap = new PseudoRandomMap(world, SPAWN_SEED);
    }

    @Override
    public void composeDecoration(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPopulationWriter writer) {
        this.randomMap.initPosSeed(pos.getMinX(), pos.getMinY(), pos.getMinZ());
        this.random.setSeed(this.randomMap.next());

        CubicGlue.proxy(writer.getGlobal()).spawnEntities(pos, writer, this.random);
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }
}
