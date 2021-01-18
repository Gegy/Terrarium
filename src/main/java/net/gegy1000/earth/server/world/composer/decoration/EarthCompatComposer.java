package net.gegy1000.earth.server.world.composer.decoration;

import dev.gegy.gengen.api.CubicPos;
import dev.gegy.gengen.api.writer.ChunkPopulationWriter;
import dev.gegy.gengen.util.SpatialRandom;
import net.gegy1000.earth.server.world.compatibility.ColumnCompatibility;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.util.Profiler;
import net.gegy1000.terrarium.server.util.ThreadedProfiler;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public final class EarthCompatComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 1569264434830154879L;

    private final SpatialRandom random;
    private final ColumnCompatibility compatibility;

    public EarthCompatComposer(WorldServer world) {
        this.random = new SpatialRandom(world, DECORATION_SEED);
        this.compatibility = new ColumnCompatibility(world);
    }

    @Override
    public void composeDecoration(TerrariumWorld terrarium, CubicPos pos, ChunkPopulationWriter writer) {
        Profiler profiler = ThreadedProfiler.get();
        Loader loader = Loader.instance();

        ModContainer activeMod = loader.activeModContainer();

        // we're invoking other mods' generation, attribute it appropriately
        loader.setActiveModContainer(null);

        try (Profiler.Handle compatibility = profiler.push("compatibility")) {
            this.compatibility.generateInColumn(terrarium, pos, world -> {
                ChunkPos columnPos = new ChunkPos(pos.getX(), pos.getZ());

                this.random.setSeed(pos.getX(), pos.getZ());

                world.firePopulateEvent(this.random, true);

                world.firePopulateEvent(this.random, PopulateChunkEvent.Populate.EventType.DUNGEON);
                world.firePopulateEvent(this.random, PopulateChunkEvent.Populate.EventType.ICE);
                world.firePopulateEvent(this.random, PopulateChunkEvent.Populate.EventType.LAKE);
                world.firePopulateEvent(this.random, PopulateChunkEvent.Populate.EventType.LAVA);

                try (Profiler.Handle decorate = profiler.push("decorate")) {
                    MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(world, this.random, columnPos));

                    try (Profiler.Handle ores = profiler.push("ores")) {
                        world.fireOreGenEvent(this.random, true);
                        world.fireOreGenEvent(this.random, OreGenEvent.GenerateMinable.EventType.DIRT);
                        world.fireOreGenEvent(this.random, OreGenEvent.GenerateMinable.EventType.GRAVEL);
                        world.fireOreGenEvent(this.random, OreGenEvent.GenerateMinable.EventType.DIORITE);
                        world.fireOreGenEvent(this.random, OreGenEvent.GenerateMinable.EventType.GRANITE);
                        world.fireOreGenEvent(this.random, OreGenEvent.GenerateMinable.EventType.ANDESITE);
                        world.fireOreGenEvent(this.random, OreGenEvent.GenerateMinable.EventType.COAL);
                        world.fireOreGenEvent(this.random, OreGenEvent.GenerateMinable.EventType.IRON);
                        world.fireOreGenEvent(this.random, OreGenEvent.GenerateMinable.EventType.GOLD);
                        world.fireOreGenEvent(this.random, OreGenEvent.GenerateMinable.EventType.REDSTONE);
                        world.fireOreGenEvent(this.random, OreGenEvent.GenerateMinable.EventType.DIAMOND);
                        world.fireOreGenEvent(this.random, OreGenEvent.GenerateMinable.EventType.LAPIS);
                        world.fireOreGenEvent(this.random, false);
                    }

                    world.fireDecorateEvent(this.random, DecorateBiomeEvent.Decorate.EventType.SAND);
                    world.fireDecorateEvent(this.random, DecorateBiomeEvent.Decorate.EventType.CLAY);
                    world.fireDecorateEvent(this.random, DecorateBiomeEvent.Decorate.EventType.SAND_PASS2);
                    world.fireDecorateEvent(this.random, DecorateBiomeEvent.Decorate.EventType.TREE);
                    world.fireDecorateEvent(this.random, DecorateBiomeEvent.Decorate.EventType.BIG_SHROOM);
                    world.fireDecorateEvent(this.random, DecorateBiomeEvent.Decorate.EventType.FLOWERS);
                    world.fireDecorateEvent(this.random, DecorateBiomeEvent.Decorate.EventType.GRASS);
                    world.fireDecorateEvent(this.random, DecorateBiomeEvent.Decorate.EventType.DEAD_BUSH);
                    world.fireDecorateEvent(this.random, DecorateBiomeEvent.Decorate.EventType.LILYPAD);
                    world.fireDecorateEvent(this.random, DecorateBiomeEvent.Decorate.EventType.SHROOM);
                    world.fireDecorateEvent(this.random, DecorateBiomeEvent.Decorate.EventType.PUMPKIN);
                    world.fireDecorateEvent(this.random, DecorateBiomeEvent.Decorate.EventType.CACTUS);
                    world.fireDecorateEvent(this.random, DecorateBiomeEvent.Decorate.EventType.LAKE_WATER);
                    world.fireDecorateEvent(this.random, DecorateBiomeEvent.Decorate.EventType.LAKE_LAVA);

                    MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(world, this.random, columnPos));
                }

                try (Profiler.Handle modded = profiler.push("modded_generators")) {
                    world.runModdedGenerators();
                }

                world.firePopulateEvent(this.random, false);
            });
        } finally {
            loader.setActiveModContainer(activeMod);
        }
    }
}
