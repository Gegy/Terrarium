package net.gegy1000.earth.server.world.ecology.vegetation;

import net.gegy1000.earth.server.event.CollectTreeGeneratorsEvent;
import net.gegy1000.earth.server.world.ecology.SoilPredicate;
import net.gegy1000.earth.server.world.feature.HookGrowthCheckFeature;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenBirchTree;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.MinecraftForge;

public final class TreeGenerators {
    private static final IBlockState JUNGLE_LOG = Blocks.LOG.getDefaultState()
            .withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
    private static final IBlockState JUNGLE_LEAF = Blocks.LEAVES.getDefaultState()
            .withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE)
            .withProperty(BlockLeaves.CHECK_DECAY, Boolean.FALSE);

    public VegetationGenerator acacia = VegetationGenerator.noop();
    public VegetationGenerator birch = VegetationGenerator.noop();
    public VegetationGenerator oak = VegetationGenerator.noop();
    public VegetationGenerator jungle = VegetationGenerator.noop();
    public VegetationGenerator bigJungle = VegetationGenerator.noop();
    public VegetationGenerator spruce = VegetationGenerator.noop();
    public VegetationGenerator pine = VegetationGenerator.noop();

    public static TreeGenerators collect() {
        TreeGenerators generators = vanilla();

        CollectTreeGeneratorsEvent event = new CollectTreeGeneratorsEvent(generators);
        MinecraftForge.EVENT_BUS.post(event);

        return generators;
    }

    private static TreeGenerators vanilla() {
        TreeGenerators vanilla = new TreeGenerators();

        WorldGenerator oak = hook(new WorldGenTrees(false));
        WorldGenerator bigOak = hook(new WorldGenBigTree(false));

        vanilla.acacia = tree(new WorldGenSavannaTree(false));
        vanilla.birch = tree(new WorldGenBirchTree(false, false));
        vanilla.oak = (world, random, pos) -> {
            if (random.nextInt(10) == 0) {
                bigOak.generate(world, random, pos);
            } else {
                oak.generate(world, random, pos);
            }
        };
        vanilla.jungle = tree(new WorldGenTrees(false, 7, JUNGLE_LOG, JUNGLE_LEAF, true));
        vanilla.bigJungle = tree(new WorldGenMegaJungle(false, 10, 20, JUNGLE_LOG, JUNGLE_LEAF));
        vanilla.pine = tree(new WorldGenTaiga1());
        vanilla.spruce = tree(new WorldGenTaiga2(false));

        return vanilla;
    }

    private static VegetationGenerator tree(WorldGenerator generator) {
        return VegetationGenerator.of(hook(generator));
    }

    private static WorldGenerator hook(WorldGenerator generator) {
        return new HookGrowthCheckFeature(generator, SoilPredicate.ANY);
    }
}
