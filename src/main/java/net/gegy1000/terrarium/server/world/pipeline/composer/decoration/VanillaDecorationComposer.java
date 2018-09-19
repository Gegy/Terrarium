package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.terrarium.server.world.chunk.CubicPos;
import net.gegy1000.terrarium.server.world.chunk.PseudoRandomMap;
import net.gegy1000.terrarium.server.world.chunk.populate.PopulateChunk;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public abstract class VanillaDecorationComposer implements DecorationComposer {
    private static final BlockPos DECORATION_CENTER = new BlockPos(16, 0, 16);

    protected final Random random;
    protected final Random horizontalRandom;
    private final PseudoRandomMap randomMap;

    protected VanillaDecorationComposer(World world, long seed) {
        this.randomMap = new PseudoRandomMap(world.getWorldInfo().getSeed(), seed);
        this.random = new Random(0);
        this.horizontalRandom = new Random(0);
    }

    @Override
    public final void composeDecoration(World world, RegionGenerationHandler regionHandler, PopulateChunk chunk) {
        CubicPos pos = chunk.getPos();

        int globalX = pos.getMinX();
        int globalZ = pos.getMinZ();

        this.randomMap.initPosSeed(globalX, globalZ);
        this.horizontalRandom.setSeed(this.randomMap.next());

        this.randomMap.initPosSeed(globalX, pos.getMinY(), globalZ);
        this.random.setSeed(this.randomMap.next());

        Biome biome = world.getChunk(pos.getX(), pos.getZ()).getBiome(DECORATION_CENTER, world.getBiomeProvider());
        this.composeDecoration(world, chunk, biome);
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }

    protected abstract void composeDecoration(World world, PopulateChunk chunk, Biome biome);
}
