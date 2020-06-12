package net.gegy1000.earth.server.world.ores;

import net.gegy1000.earth.server.world.composer.decoration.OreDecorationComposer;
import net.minecraft.block.BlockStone;
import net.minecraft.init.Blocks;

public final class VanillaOres {
    public static final OreConfig DIRT = OreConfig.builder()
            .ore(Blocks.DIRT.getDefaultState()).size(33)
            .distribution(OreDistribution.vanillaUniform(10, 256))
            .build();
    public static final OreConfig GRAVEL = OreConfig.builder()
            .ore(Blocks.GRAVEL.getDefaultState()).size(33)
            .distribution(OreDistribution.vanillaUniform(8, 256))
            .build();

    public static final OreConfig GRANITE = OreConfig.builder()
            .ore(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE)).size(33)
            .distribution(OreDistribution.vanillaUniform(10, 80))
            .build();
    public static final OreConfig DIORITE = OreConfig.builder()
            .ore(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE)).size(33)
            .distribution(OreDistribution.vanillaUniform(10, 80))
            .build();
    public static final OreConfig ANDESITE = OreConfig.builder()
            .ore(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE)).size(33)
            .distribution(OreDistribution.vanillaUniform(10, 80))
            .build();

    public static final OreConfig COAL = OreConfig.builder()
            .ore(Blocks.COAL_ORE.getDefaultState()).size(17)
            .distribution(OreDistribution.vanillaUniform(20, 128))
            .build();
    public static final OreConfig IRON = OreConfig.builder()
            .ore(Blocks.IRON_ORE.getDefaultState()).size(9)
            .distribution(OreDistribution.vanillaUniform(20, 64))
            .build();
    public static final OreConfig GOLD = OreConfig.builder()
            .ore(Blocks.GOLD_ORE.getDefaultState()).size(9)
            .distribution(OreDistribution.vanillaUniform(2, 32))
            .build();
    public static final OreConfig REDSTONE = OreConfig.builder()
            .ore(Blocks.REDSTONE_ORE.getDefaultState()).size(8)
            .distribution(OreDistribution.vanillaUniform(8, 16))
            .build();
    public static final OreConfig DIAMOND = OreConfig.builder()
            .ore(Blocks.DIAMOND_ORE.getDefaultState()).size(8)
            .distribution(OreDistribution.vanillaUniform(1, 16))
            .build();
    public static final OreConfig LAPIS = OreConfig.builder()
            .ore(Blocks.LAPIS_ORE.getDefaultState()).size(7)
            .distribution(OreDistribution.vanillaBand(1, 16, 16))
            .build();

    public static final OreConfig EMERALD = OreConfig.builder()
            .ore(Blocks.EMERALD_ORE.getDefaultState()).size(1)
            .distribution(OreDistribution.uniform(1.0, -30))
            .build();

    public static void addTo(OreDecorationComposer composer) {
        composer.add(DIRT, GRAVEL);
        composer.add(GRANITE, DIORITE, ANDESITE);
        composer.add(COAL, IRON, GOLD, REDSTONE);
        composer.add(DIAMOND, LAPIS, EMERALD);
    }
}
