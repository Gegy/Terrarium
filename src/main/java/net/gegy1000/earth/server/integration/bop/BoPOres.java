package net.gegy1000.earth.server.integration.bop;

import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.api.enums.BOPGems;
import biomesoplenty.common.block.BlockBOPGem;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.composer.decoration.OreDecorationComposer;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.gegy1000.earth.server.world.ores.OreConfig;
import net.gegy1000.earth.server.world.ores.OreDistribution;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;

import static net.gegy1000.earth.server.world.Climate.MIN_FREEZE;

public final class BoPOres {
    private static final FloatRaster.Sampler MEAN_TEMPERATURE = FloatRaster.sampler(EarthData.MEAN_TEMPERATURE);
    private static final FloatRaster.Sampler MIN_TEMPERATURE = FloatRaster.sampler(EarthData.MIN_TEMPERATURE);
    private static final ShortRaster.Sampler ANNUAL_RAINFALL = ShortRaster.sampler(EarthData.ANNUAL_RAINFALL);
    private static final FloatRaster.Sampler ELEVATION = FloatRaster.sampler(EarthData.ELEVATION_METERS);
    private static final EnumRaster.Sampler<Cover> COVER = EnumRaster.sampler(EarthData.COVER, Cover.NO);

    public static final OreConfig TANZANITE = OreConfig.builder()
            .ore(BOPBlocks.gem_ore.getDefaultState().withProperty(BlockBOPGem.VARIANT, BOPGems.TANZANITE))
            .distribution(OreDistribution.vanillaUniform(12, 32))
            .select((data, x, z) -> MIN_TEMPERATURE.sample(data, x, z) < MIN_FREEZE)
            .build();

    public static final OreConfig SAPPHIRE = OreConfig.builder()
            .ore(BOPBlocks.gem_ore.getDefaultState().withProperty(BlockBOPGem.VARIANT, BOPGems.SAPPHIRE))
            .distribution(OreDistribution.vanillaUniform(12, 32))
            .select((data, x, z) -> ELEVATION.sample(data, x, z) < 0.0F)
            .build();

    public static final OreConfig RUBY = OreConfig.builder()
            .ore(BOPBlocks.gem_ore.getDefaultState().withProperty(BlockBOPGem.VARIANT, BOPGems.RUBY))
            .distribution(OreDistribution.vanillaUniform(12, 32))
            .select((data, x, z) -> MEAN_TEMPERATURE.sample(data, x, z) > 20.0F)
            .build();

    public static final OreConfig TOPAZ = OreConfig.builder()
            .ore(BOPBlocks.gem_ore.getDefaultState().withProperty(BlockBOPGem.VARIANT, BOPGems.TOPAZ))
            .distribution(OreDistribution.vanillaUniform(12, 32))
            .select((data, x, z) -> ANNUAL_RAINFALL.sample(data, x, z) > 1200.0F)
            .build();

    public static final OreConfig AMBER = OreConfig.builder()
            .ore(BOPBlocks.gem_ore.getDefaultState().withProperty(BlockBOPGem.VARIANT, BOPGems.AMBER))
            .distribution(OreDistribution.vanillaUniform(12, 32))
            .select((data, x, z) -> COVER.sample(data, x, z).is(CoverMarkers.FOREST))
            .build();

    public static final OreConfig PERIDOT = OreConfig.builder()
            .ore(BOPBlocks.gem_ore.getDefaultState().withProperty(BlockBOPGem.VARIANT, BOPGems.PERIDOT))
            .distribution(OreDistribution.vanillaUniform(12, 32))
            .select((data, x, z) -> {
                Cover cover = COVER.sample(data, x, z);
                return cover.is(CoverMarkers.PLAINS) || cover.is(CoverMarkers.BARREN);
            })
            .build();

    public static final OreConfig MALACHITE = OreConfig.builder()
            .ore(BOPBlocks.gem_ore.getDefaultState().withProperty(BlockBOPGem.VARIANT, BOPGems.MALACHITE))
            .distribution(OreDistribution.vanillaUniform(12, 32))
            .select((data, x, z) -> COVER.sample(data, x, z).is(CoverMarkers.FLOODED))
            .build();

    public static void addTo(OreDecorationComposer composer) {
        composer.add(TANZANITE, SAPPHIRE, RUBY, TOPAZ, AMBER, PERIDOT, MALACHITE);
    }
}
