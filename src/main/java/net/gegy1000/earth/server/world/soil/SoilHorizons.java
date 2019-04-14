package net.gegy1000.earth.server.world.soil;

import net.gegy1000.earth.server.compat.BoPCompat;
import net.gegy1000.earth.server.world.soil.horizon.BinaryPatchedHorizonConfig;
import net.gegy1000.earth.server.world.soil.horizon.SimpleHorizonConfig;
import net.gegy1000.earth.server.world.soil.horizon.SoilHorizonConfig;
import net.gegy1000.earth.server.world.soil.horizon.SprinkledHorizonConfig;
import net.minecraft.block.BlockDirt;
import net.minecraft.init.Blocks;

public class SoilHorizons {
    public static final SoilHorizonConfig SNOW_HORIZON = SimpleHorizonConfig.of(Blocks.SNOW);

    public static final SoilHorizonConfig GRASS_HORIZON = SimpleHorizonConfig.of(Blocks.GRASS);
    public static final SoilHorizonConfig DIRT_HORIZON = SimpleHorizonConfig.of(Blocks.DIRT);
    public static final SoilHorizonConfig SAND_HORIZON = SimpleHorizonConfig.of(Blocks.SAND);
    public static final SoilHorizonConfig GRAVEL_HORIZON = SimpleHorizonConfig.of(Blocks.GRAVEL);
    public static final SoilHorizonConfig CLAY_HORIZON = SimpleHorizonConfig.of(Blocks.CLAY);
    public static final SoilHorizonConfig STONE_HORIZON = SimpleHorizonConfig.of(Blocks.STONE);
    public static final SoilHorizonConfig COARSE_DIRT_HORIZON = SimpleHorizonConfig.of(
            Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)
    );

    public static final SoilHorizonConfig LOAMY_DIRT_HORIZON = SimpleHorizonConfig.of(
            BoPCompat.loamyDirt().orElse(Blocks.DIRT.getDefaultState())
    );

    public static final SoilHorizonConfig SANDY_DIRT_HORIZON = BoPCompat.sandyDirt().map(SimpleHorizonConfig::of)
            .orElseGet(() -> BinaryPatchedHorizonConfig.of(
                    Blocks.SAND.getDefaultState(),
                    Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)
            ));

    public static final SoilHorizonConfig GRAVELLY_DIRT_HORIZON = BoPCompat.sandyDirt().map(SimpleHorizonConfig::of)
            .orElseGet(() -> BinaryPatchedHorizonConfig.of(
                    Blocks.GRAVEL,
                    Blocks.DIRT
            ));

    public static final SoilHorizonConfig SILTY_DIRT_HORIZON = BoPCompat.siltyDirt().map(SimpleHorizonConfig::of)
            .orElseGet(() -> BinaryPatchedHorizonConfig.of(Blocks.DIRT, Blocks.CLAY, -0.5));

    public static final SoilHorizonConfig LOAMY_GRASS_HORIZON = SimpleHorizonConfig.of(
            BoPCompat.loamyGrass().orElse(Blocks.GRASS.getDefaultState())
    );

    public static final SoilHorizonConfig SANDY_GRASS_HORIZON = BoPCompat.sandyDirt().map(SimpleHorizonConfig::of)
            .orElseGet(() -> BinaryPatchedHorizonConfig.of(
                    Blocks.SAND,
                    Blocks.GRASS
            ));

    public static final SoilHorizonConfig GRAVELLY_GRASS_HORIZON = BoPCompat.sandyDirt().map(SimpleHorizonConfig::of)
            .orElseGet(() -> BinaryPatchedHorizonConfig.of(
                    Blocks.GRAVEL,
                    Blocks.GRASS
            ));

    public static final SoilHorizonConfig SILTY_GRASS_HORIZON = BoPCompat.siltyGrass().map(SimpleHorizonConfig::of)
            .orElseGet(() -> BinaryPatchedHorizonConfig.of(Blocks.GRASS, Blocks.CLAY, -0.5));

    public static final SoilHorizonConfig PODZOL_HORIZON = SimpleHorizonConfig.of(
            Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL)
    );

    public static final SoilHorizonConfig PLINTHOSOL_SUBSOIL = SprinkledHorizonConfig.of(
            SILTY_GRASS_HORIZON,
            Blocks.IRON_ORE.getDefaultState(),
            6
    );
}
