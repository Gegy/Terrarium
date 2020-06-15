package net.gegy1000.earth.server.world.composer.structure;

import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.compatibility.ColumnCompatibilityWorld;
import net.gegy1000.earth.server.world.composer.structure.placement.CellStructurePlacement;
import net.gegy1000.earth.server.world.composer.structure.placement.StructurePlacement;
import net.gegy1000.gengen.api.HeightFunction;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.util.Lazy;
import net.gegy1000.terrarium.server.world.composer.structure.StructureComposer;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.WoodlandMansion;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class MansionStructureComposer {
    private static final List<Biome> MANSION_BIOMES = Arrays.asList(
            Biomes.FOREST, Biomes.BIRCH_FOREST,
            Biomes.JUNGLE, Biomes.TAIGA,
            Biomes.ROOFED_FOREST
    );

    public static StructureComposer create(World world, HeightFunction surfaceFunction) {
        StructurePlacement placement = new CellStructurePlacement(80, 20, 10387319)
                .setPredicate(MansionStructureComposer::canSpawnAt);

        CompatChunkGenerator overworld = new CompatChunkGenerator(world);

        return ColumnStructureComposer.builder()
                .setStructureName("Mansion")
                .setPlacement(placement)
                .setSurfaceFunction(surfaceFunction)
                .setStartConstructor((w, r, x, z) -> makeStart(overworld, w, r, x, z))
                .build(world);
    }

    private static StructureStart makeStart(CompatChunkGenerator overworld, World world, Random random, int chunkX, int chunkZ) {
        if (world instanceof ColumnCompatibilityWorld) {
            overworld.setupAt(((ColumnCompatibilityWorld) world).getMinY());
        }
        return new WoodlandMansion.Start(world, overworld, random, chunkX, chunkZ);
    }

    private static boolean canSpawnAt(World world, int chunkX, int chunkZ) {
        int x = (chunkX << 4) + 8;
        int z = (chunkZ << 4) + 8;
        return world.getBiomeProvider().areBiomesViable(x, z, 16, MANSION_BIOMES);
    }

    static final class CompatChunkGenerator extends ChunkGeneratorOverworld {
        private final Lazy<Optional<TerrariumWorld>> terrarium;
        private int minY;

        CompatChunkGenerator(World world) {
            super(world, world.getWorldInfo().getSeed(), false, null);
            this.terrarium = Lazy.ofCapability(world, TerrariumCapabilities.world());
        }

        void setupAt(int minY) {
            this.minY = minY;
        }

        @Override
        public void setBlocksInChunk(int x, int z, ChunkPrimer primer) {
            // the mansion generator only uses findGroundBlockIdx: so we only need to set blocks at the surface

            Optional<ShortRaster> heightOpt = this.terrarium.get()
                    .flatMap(terrarium -> terrarium.getDataCache().joinData(x, z, EarthData.TERRAIN_HEIGHT));

            heightOpt.ifPresent(heightRaster -> {
                for (int localZ = 0; localZ < 16; localZ++) {
                    for (int localX = 0; localX < 16; localX++) {
                        int height = heightRaster.get(localX, localZ) - this.minY;
                        if (height >= 0 && height < 255) {
                            primer.setBlockState(localX, height, localZ, STONE);
                        }
                    }
                }
            });
        }
    }
}
