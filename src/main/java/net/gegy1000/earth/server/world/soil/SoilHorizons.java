package net.gegy1000.earth.server.world.soil;

import net.gegy1000.earth.server.compat.BoPCompat;
import net.gegy1000.earth.server.world.soil.horizon.EvenDistributionHorizonConfig;
import net.gegy1000.earth.server.world.soil.horizon.MixedClayHorizonConfig;
import net.gegy1000.earth.server.world.soil.horizon.ScatteredOreHorizonConfig;
import net.gegy1000.earth.server.world.soil.horizon.SimpleHorizonConfig;
import net.gegy1000.earth.server.world.soil.horizon.SoilHorizonConfig;
import net.minecraft.block.BlockConcretePowder;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSand;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;

public class SoilHorizons {
    public static final SoilHorizonConfig SNOW_HORIZON = SimpleHorizonConfig.of(Blocks.SNOW);

    public static final SoilHorizonConfig GRASS_HORIZON = SimpleHorizonConfig.of(Blocks.GRASS);
    public static final SoilHorizonConfig DIRT_HORIZON = SimpleHorizonConfig.of(Blocks.DIRT);
    public static final SoilHorizonConfig SAND_HORIZON = SimpleHorizonConfig.of(Blocks.SAND);
    public static final SoilHorizonConfig GRAVEL_HORIZON = SimpleHorizonConfig.of(Blocks.GRAVEL);
    public static final SoilHorizonConfig CLAY_HORIZON = SimpleHorizonConfig.of(Blocks.CLAY);
    public static final SoilHorizonConfig STONE_HORIZON = SimpleHorizonConfig.of(Blocks.STONE);

    public static final SoilHorizonConfig LOAMY_DIRT_HORIZON = SimpleHorizonConfig.of(
            BoPCompat.loamyDirt().orElse(Blocks.DIRT.getDefaultState())
    );

    public static final SoilHorizonConfig SANDY_DIRT_HORIZON = SimpleHorizonConfig.of(
            BoPCompat.sandyDirt().orElse(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT))
    );

    public static final SoilHorizonConfig SILTY_DIRT_HORIZON = BoPCompat.siltyDirt().map(SimpleHorizonConfig::of)
            .orElseGet(() -> EvenDistributionHorizonConfig.of(Blocks.DIRT, Blocks.CLAY));

    public static final SoilHorizonConfig LOAMY_GRASS_HORIZON = SimpleHorizonConfig.of(
            BoPCompat.loamyGrass().orElse(Blocks.GRASS.getDefaultState())
    );

    public static final SoilHorizonConfig SANDY_GRASS_HORIZON = SimpleHorizonConfig.of(
            BoPCompat.sandyGrass().orElse(Blocks.GRASS.getDefaultState())
    );

    public static final SoilHorizonConfig SILTY_GRASS_HORIZON = BoPCompat.siltyGrass().map(SimpleHorizonConfig::of)
            .orElseGet(() -> EvenDistributionHorizonConfig.of(Blocks.GRASS, Blocks.CLAY));

    public static final SoilHorizonConfig PODZOL_HORIZON = SimpleHorizonConfig.of(
            Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL)
    );

    public static final SoilHorizonConfig ACRISOL_HORIZON = new MixedClayHorizonConfig(
            Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND),
            Blocks.CLAY.getDefaultState()
    );

    public static final SoilHorizonConfig BLACK_SOIL_HORIZON = SimpleHorizonConfig.of(
            Blocks.CONCRETE_POWDER.getDefaultState().withProperty(BlockConcretePowder.COLOR, EnumDyeColor.BLACK)
    );

    public static final SoilHorizonConfig LIXISOL_GRASS_HORIZON = new MixedClayHorizonConfig(
            Blocks.GRASS.getDefaultState(),
            Blocks.CLAY.getDefaultState()
    );

    public static final SoilHorizonConfig LIXISOL_DIRT_HORIZON = new MixedClayHorizonConfig(
            Blocks.DIRT.getDefaultState(),
            Blocks.CLAY.getDefaultState()
    );

    public static final SoilHorizonConfig LUVISOL_DIRT_HORIZON = new MixedClayHorizonConfig(
            Blocks.DIRT.getDefaultState(),
            Blocks.CLAY.getDefaultState(),
            6
    );

    public static final SoilHorizonConfig NITISOL_HORIZON = new MixedClayHorizonConfig(
            Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND),
            Blocks.CLAY.getDefaultState(),
            6
    );

    public static final SoilHorizonConfig COARSE_CLAY_HORIZON = new MixedClayHorizonConfig(
            Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT),
            Blocks.CLAY.getDefaultState(),
            6
    );

    public static final SoilHorizonConfig PLINTHOSOL_SUBSOIL = ScatteredOreHorizonConfig.of(
            Blocks.IRON_ORE,
            6,
            Blocks.CLAY,
            Blocks.DIRT
    );
}
