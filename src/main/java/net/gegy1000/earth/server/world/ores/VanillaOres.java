package net.gegy1000.earth.server.world.ores;

import net.gegy1000.earth.server.world.composer.OreDecorationComposer;
import net.minecraft.block.BlockStone;
import net.minecraft.init.Blocks;

public final class VanillaOres {
    public final OreConfig dirt = OreConfig.builder()
            .ore(Blocks.DIRT.getDefaultState()).size(33)
            .distribution(OreDistribution.vanillaUniform(10, 256))
            .build();
    public final OreConfig gravel = OreConfig.builder()
            .ore(Blocks.GRAVEL.getDefaultState()).size(33)
            .distribution(OreDistribution.vanillaUniform(8, 256))
            .build();

    public final OreConfig granite = OreConfig.builder()
            .ore(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE)).size(33)
            .distribution(OreDistribution.vanillaUniform(10, 80))
            .build();
    public final OreConfig diorite = OreConfig.builder()
            .ore(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE)).size(33)
            .distribution(OreDistribution.vanillaUniform(10, 80))
            .build();
    public final OreConfig andesite = OreConfig.builder()
            .ore(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE)).size(33)
            .distribution(OreDistribution.vanillaUniform(10, 80))
            .build();

    public final OreConfig coal = OreConfig.builder()
            .ore(Blocks.COAL_ORE.getDefaultState()).size(17)
            .distribution(OreDistribution.vanillaUniform(20, 128))
            .build();
    public final OreConfig iron = OreConfig.builder()
            .ore(Blocks.IRON_ORE.getDefaultState()).size(9)
            .distribution(OreDistribution.vanillaUniform(20, 64))
            .build();
    public final OreConfig gold = OreConfig.builder()
            .ore(Blocks.GOLD_ORE.getDefaultState()).size(9)
            .distribution(OreDistribution.vanillaUniform(2, 32))
            .build();
    public final OreConfig redstone = OreConfig.builder()
            .ore(Blocks.REDSTONE_ORE.getDefaultState()).size(8)
            .distribution(OreDistribution.vanillaUniform(8, 16))
            .build();
    public final OreConfig diamond = OreConfig.builder()
            .ore(Blocks.DIAMOND_ORE.getDefaultState()).size(8)
            .distribution(OreDistribution.vanillaUniform(1, 16))
            .build();
    public final OreConfig lapis = OreConfig.builder()
            .ore(Blocks.LAPIS_ORE.getDefaultState()).size(7)
            .distribution(OreDistribution.vanillaBand(1, 16, 16))
            .build();

    public final OreConfig emerald = OreConfig.builder()
            .ore(Blocks.EMERALD_ORE.getDefaultState()).size(1)
            .distribution(OreDistribution.uniform(1.0, -30))
            .build();

    private VanillaOres() {
    }

    public static VanillaOres get() {
        return new VanillaOres();
    }

    public void addTo(OreDecorationComposer composer) {
        composer.add(this.dirt, this.gravel);
        composer.add(this.granite, this.diorite, this.andesite);
        composer.add(this.coal, this.iron, this.gold, this.redstone);
        composer.add(this.diamond, this.lapis, this.emerald);
    }
}
