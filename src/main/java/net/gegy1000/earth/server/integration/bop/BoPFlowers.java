package net.gegy1000.earth.server.integration.bop;

import biomesoplenty.api.enums.BOPFlowers;
import biomesoplenty.common.block.BlockBOPFlower;
import net.gegy1000.earth.server.world.ecology.vegetation.VegetationGenerator;
import net.gegy1000.earth.server.world.feature.FlowersFeature;
import net.minecraft.block.state.IBlockState;

public final class BoPFlowers {
    public static final VegetationGenerator CLOVER = flower(BOPFlowers.CLOVER);
    public static final VegetationGenerator SWAMPFLOWER = flower(BOPFlowers.SWAMPFLOWER);
    public static final VegetationGenerator DEATHBLOOM = flower(BOPFlowers.DEATHBLOOM);
    public static final VegetationGenerator GLOWFLOWER = flower(BOPFlowers.GLOWFLOWER);
    public static final VegetationGenerator BLUE_HYDRANGEA = flower(BOPFlowers.BLUE_HYDRANGEA);
    public static final VegetationGenerator ORANGE_COSMOS = flower(BOPFlowers.ORANGE_COSMOS);
    public static final VegetationGenerator PINK_DAFFODIL = flower(BOPFlowers.PINK_DAFFODIL);
    public static final VegetationGenerator WILDFLOWER = flower(BOPFlowers.WILDFLOWER);
    public static final VegetationGenerator VIOLET = flower(BOPFlowers.VIOLET);
    public static final VegetationGenerator WHITE_ANEMONE = flower(BOPFlowers.WHITE_ANEMONE);
    public static final VegetationGenerator BROMELIAD = flower(BOPFlowers.BROMELIAD);
    public static final VegetationGenerator PINK_HIBISCUS = flower(BOPFlowers.PINK_HIBISCUS);
    public static final VegetationGenerator LILY_OF_THE_VALLEY = flower(BOPFlowers.LILY_OF_THE_VALLEY);
    public static final VegetationGenerator LAVENDER = flower(BOPFlowers.LAVENDER);
    public static final VegetationGenerator GOLDENROD = flower(BOPFlowers.GOLDENROD);
    public static final VegetationGenerator BLUEBELLS = flower(BOPFlowers.BLUEBELLS);
    public static final VegetationGenerator ICY_IRIS = flower(BOPFlowers.ICY_IRIS);

    private static VegetationGenerator flower(BOPFlowers flowers) {
        IBlockState state = BlockBOPFlower.paging.getVariantState(flowers);
        return VegetationGenerator.of(new FlowersFeature(state));
    }
}
