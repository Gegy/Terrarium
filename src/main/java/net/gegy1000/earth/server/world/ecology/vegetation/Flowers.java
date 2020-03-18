package net.gegy1000.earth.server.world.ecology.vegetation;

import net.gegy1000.earth.server.world.feature.FlowersFeature;
import net.gegy1000.earth.server.world.feature.TallFlowerFeature;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.state.IBlockState;

public final class Flowers {
    public static final VegetationGenerator DANDELION = flower(BlockFlower.EnumFlowerType.DANDELION);
    public static final VegetationGenerator ALLIUM = flower(BlockFlower.EnumFlowerType.ALLIUM);
    public static final VegetationGenerator BLUE_ORCHID = flower(BlockFlower.EnumFlowerType.BLUE_ORCHID);
    public static final VegetationGenerator HOUSTONIA = flower(BlockFlower.EnumFlowerType.HOUSTONIA);
    public static final VegetationGenerator ORANGE_TULIP = flower(BlockFlower.EnumFlowerType.ORANGE_TULIP);
    public static final VegetationGenerator OXEYE_DAISY = flower(BlockFlower.EnumFlowerType.OXEYE_DAISY);
    public static final VegetationGenerator PINK_TULIP = flower(BlockFlower.EnumFlowerType.PINK_TULIP);
    public static final VegetationGenerator POPPY = flower(BlockFlower.EnumFlowerType.POPPY);
    public static final VegetationGenerator RED_TULIP = flower(BlockFlower.EnumFlowerType.RED_TULIP);
    public static final VegetationGenerator WHITE_TULIP = flower(BlockFlower.EnumFlowerType.WHITE_TULIP);

    public static final VegetationGenerator SUNFLOWER = tallFlower(BlockDoublePlant.EnumPlantType.SUNFLOWER);
    public static final VegetationGenerator SYRINGA = tallFlower(BlockDoublePlant.EnumPlantType.SYRINGA);
    public static final VegetationGenerator ROSE = tallFlower(BlockDoublePlant.EnumPlantType.ROSE);
    public static final VegetationGenerator PAEONIA = tallFlower(BlockDoublePlant.EnumPlantType.PAEONIA);

    private static VegetationGenerator flower(BlockFlower.EnumFlowerType type) {
        BlockFlower block = type.getBlockType().getBlock();
        IBlockState state = block.getDefaultState().withProperty(block.getTypeProperty(), type);
        return VegetationGenerator.of(new FlowersFeature(state));
    }

    private static VegetationGenerator tallFlower(BlockDoublePlant.EnumPlantType type) {
        return VegetationGenerator.of(new TallFlowerFeature(type));
    }
}
