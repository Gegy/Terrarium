package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.cubicglue.util.PseudoRandomMap;
import net.gegy1000.cubicglue.util.VanillaGeneratorCache;
import net.gegy1000.earth.server.EarthDecorationEventHandler;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
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
    public void composeDecoration(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPopulationWriter writer) {
        List<IWorldGenerator> capturedGenerators = EarthDecorationEventHandler.capturedGenerators;

        if (capturedGenerators != null && !capturedGenerators.isEmpty()) {
            World world = writer.getGlobal();

            IChunkProvider provider = world.getChunkProvider();
            IChunkGenerator generator = VanillaGeneratorCache.getGenerator(world);
            if (generator == null) {
                return;
            }

            this.randomMap.initPosSeed(pos.getMinX(), pos.getMinZ());
            this.horizontalRandom.setSeed(this.randomMap.next());

            long chunkSeed = this.horizontalRandom.nextLong();

            for (IWorldGenerator worldGenerator : capturedGenerators) {
                this.horizontalRandom.setSeed(chunkSeed);
                worldGenerator.generate(this.horizontalRandom, pos.getX(), pos.getZ(), world, generator, provider);
            }
        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }
}
