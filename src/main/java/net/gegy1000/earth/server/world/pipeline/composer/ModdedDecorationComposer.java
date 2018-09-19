package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.earth.server.EarthDecorationEventHandler;
import net.gegy1000.terrarium.server.world.chunk.CubicPos;
import net.gegy1000.terrarium.server.world.chunk.PseudoRandomMap;
import net.gegy1000.terrarium.server.world.chunk.populate.PopulateChunk;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.List;
import java.util.Random;

public class ModdedDecorationComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 6926778772467445428L;

    private final PseudoRandomMap randomMap;
    private final Random horizontalRandom;

    public ModdedDecorationComposer(World world) {
        this.randomMap = new PseudoRandomMap(world, DECORATION_SEED);
        this.horizontalRandom = new Random(0);
    }

    @Override
    public void composeDecoration(World world, RegionGenerationHandler regionHandler, PopulateChunk chunk) {
        List<IWorldGenerator> capturedGenerators = EarthDecorationEventHandler.capturedGenerators;
        if (capturedGenerators != null && !capturedGenerators.isEmpty()) {
            IChunkProvider chunkProvider = world.getChunkProvider();

            CubicPos pos = chunk.getPos();
            this.randomMap.initPosSeed(pos.getMinX(), pos.getMinZ());
            this.horizontalRandom.setSeed(this.randomMap.next());

            long chunkSeed = this.horizontalRandom.nextLong();

            for (IWorldGenerator worldGenerator : capturedGenerators) {
                this.horizontalRandom.setSeed(chunkSeed);
                // TODO: Generator
                worldGenerator.generate(this.horizontalRandom, pos.getX(), pos.getZ(), world, null, chunkProvider);
            }
        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }
}
