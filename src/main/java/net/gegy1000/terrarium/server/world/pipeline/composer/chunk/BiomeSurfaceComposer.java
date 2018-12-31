package net.gegy1000.terrarium.server.world.pipeline.composer.chunk;

import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.BlockState;
import net.minecraft.class_2919;
import net.minecraft.util.math.noise.NoiseSampler;
import net.minecraft.util.math.noise.OctaveSimplexNoiseSampler;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPos;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.Random;

public class BiomeSurfaceComposer<C extends TerrariumGeneratorConfig> implements ChunkComposer<C> {
    private final RegionComponentType<BiomeRasterTile> biomeComponent;
    private final NoiseSampler surfaceNoise;

    public BiomeSurfaceComposer(IWorld world, RegionComponentType<BiomeRasterTile> biomeComponent) {
        this.biomeComponent = biomeComponent;

        Random random = new Random(world.getLevelProperties().getSeed());
        this.surfaceNoise = new OctaveSimplexNoiseSampler(random, 4);
    }

    @Override
    public void compose(ChunkGenerator<C> generator, Chunk chunk, class_2919 random, RegionGenerationHandler regionHandler) {
        ChunkPos chunkPos = chunk.getPos();

        C settings = generator.getSettings();
        BlockState defaultBlock = settings.getDefaultBlock();
        BlockState defaultFluid = settings.getDefaultFluid();
        long seed = generator.getSeed();

        int originX = chunkPos.getXStart();
        int originZ = chunkPos.getZStart();

        BiomeRasterTile biomeTile = regionHandler.getCachedChunkRaster(this.biomeComponent);

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int globalX = originX + localX;
                int globalZ = originZ + localZ;
                int height = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, localX, localZ) + 1;
                double surfaceNoise = this.surfaceNoise.sample(globalX * 0.0625, globalZ * 0.0625, 0.0625, localX * 0.0625);

                Biome biome = biomeTile.get(localX, localZ);
                biome.buildSurface(random, chunk, globalX, globalZ, height, surfaceNoise, defaultBlock, defaultFluid, 63, seed);
            }
        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[] { this.biomeComponent };
    }
}
