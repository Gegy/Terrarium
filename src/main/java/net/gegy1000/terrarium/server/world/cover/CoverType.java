package net.gegy1000.terrarium.server.world.cover;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.generator.BareCover;
import net.gegy1000.terrarium.server.world.cover.generator.BeachCover;
import net.gegy1000.terrarium.server.world.cover.generator.BroadleafEvergreenCover;
import net.gegy1000.terrarium.server.world.cover.generator.ClosedBroadleafDeciduousCover;
import net.gegy1000.terrarium.server.world.cover.generator.ClosedNeedleleafEvergreenCover;
import net.gegy1000.terrarium.server.world.cover.generator.CroplandWithVegetationCover;
import net.gegy1000.terrarium.server.world.cover.generator.DebugCover;
import net.gegy1000.terrarium.server.world.cover.generator.FloodedGrasslandCover;
import net.gegy1000.terrarium.server.world.cover.generator.ForestShrublandWithGrassCover;
import net.gegy1000.terrarium.server.world.cover.generator.FreshFloodedForestCover;
import net.gegy1000.terrarium.server.world.cover.generator.GrassWithForestShrublandCover;
import net.gegy1000.terrarium.server.world.cover.generator.GrasslandCover;
import net.gegy1000.terrarium.server.world.cover.generator.IrrigatedCropsCover;
import net.gegy1000.terrarium.server.world.cover.generator.MixedBroadNeedleleafCover;
import net.gegy1000.terrarium.server.world.cover.generator.OpenBroadleafDeciduousCover;
import net.gegy1000.terrarium.server.world.cover.generator.OpenNeedleleafCover;
import net.gegy1000.terrarium.server.world.cover.generator.RainfedCropsCover;
import net.gegy1000.terrarium.server.world.cover.generator.SalineFloodedForestCover;
import net.gegy1000.terrarium.server.world.cover.generator.ShrublandCover;
import net.gegy1000.terrarium.server.world.cover.generator.SnowCover;
import net.gegy1000.terrarium.server.world.cover.generator.SparseVegetationCover;
import net.gegy1000.terrarium.server.world.cover.generator.UrbanCover;
import net.gegy1000.terrarium.server.world.cover.generator.VegetationWithCroplandCover;
import net.gegy1000.terrarium.server.world.cover.generator.WaterCover;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

import java.lang.reflect.Constructor;
import java.util.function.Function;

public enum CoverType {
    IRRIGATED_CROPS(11, CoverBiomeSelectors.BROADLEAF_FOREST_SELECTOR, IrrigatedCropsCover.class, 0xAAEFEF),
    RAINFED_CROPS(14, CoverBiomeSelectors.BROADLEAF_FOREST_SELECTOR, RainfedCropsCover.class, 0xFFFF63),
    CROPLAND_WITH_VEGETATION(20, CoverBiomeSelectors.FOREST_SHRUBLAND_SELECTOR, CroplandWithVegetationCover.class, 0xDCEF63),
    VEGETATION_WITH_CROPLAND(30, CoverBiomeSelectors.FOREST_SHRUBLAND_SELECTOR, VegetationWithCroplandCover.class, 0xCDCD64),
    BROADLEAF_EVERGREEN(40, CoverBiomeSelectors.BROADLEAF_FOREST_SELECTOR, BroadleafEvergreenCover.class, 0x006300),
    CLOSED_BROADLEAF_DECIDUOUS(50, CoverBiomeSelectors.BROADLEAF_FOREST_SELECTOR, ClosedBroadleafDeciduousCover.class, 0x009F00),
    OPEN_BROADLEAF_DECIDUOUS(60, CoverBiomeSelectors.BROADLEAF_FOREST_SELECTOR, OpenBroadleafDeciduousCover.class, 0xAAC700),
    CLOSED_NEEDLELEAF_EVERGREEN(70, CoverBiomeSelectors.NEEDLELEAF_FOREST_SELECTOR, ClosedNeedleleafEvergreenCover.class, 0x003B00),
    OPEN_NEEDLELEAF(90, CoverBiomeSelectors.NEEDLELEAF_FOREST_SELECTOR, OpenNeedleleafCover.class, 0x286300),
    MIXED_BROAD_NEEDLELEAF(100, Biomes.FOREST, MixedBroadNeedleleafCover.class, 0x788300),
    FOREST_SHRUBLAND_WITH_GRASS(110, CoverBiomeSelectors.FOREST_SHRUBLAND_SELECTOR, ForestShrublandWithGrassCover.class, 0x8D9F00),
    GRASS_WITH_FOREST_SHRUBLAND(120, CoverBiomeSelectors.FOREST_SHRUBLAND_SELECTOR, GrassWithForestShrublandCover.class, 0xBD9500),
    SHRUBLAND(130, CoverBiomeSelectors.SHRUBLAND_SELECTOR, ShrublandCover.class, 0x956300),
    GRASSLAND(140, CoverBiomeSelectors.GRASSLAND_SELECTOR, GrasslandCover.class, 0xFFB431),
    SPARSE_VEGETATION(150, CoverBiomeSelectors.GRASSLAND_SELECTOR, SparseVegetationCover.class, 0xFFEBAE),
    FRESH_FLOODED_FOREST(160, CoverBiomeSelectors.FLOODED_SELECTOR, FreshFloodedForestCover.class, 0x00785A),
    SALINE_FLOODED_FOREST(170, CoverBiomeSelectors.SALINE_FLOODED_SELECTOR, SalineFloodedForestCover.class, 0x009578),
    FLOODED_GRASSLAND(180, CoverBiomeSelectors.FLOODED_SELECTOR, FloodedGrasslandCover.class, 0x00DC83),
    URBAN(190, Biomes.PLAINS, UrbanCover.class, 0xC31300),
    BARE(200, Biomes.DESERT, BareCover.class, 0xFFF5D6),
    WATER(210, zone -> Biomes.OCEAN, WaterCover.class, 0.0, false, 0x0046C7),
    SNOW(220, Biomes.ICE_PLAINS, SnowCover.class, 0xFFFFFF),
    NO_DATA(0, Biomes.PLAINS, BareCover.class, 0),
    BEACH(-1, zone -> Biomes.BEACH, BeachCover.class, 0.35, false, 0xFFF5D6),
    PROCESSING(-1, Biomes.DEFAULT, BareCover.class, 0),
    DEBUG(-1, zone -> Biomes.DEFAULT, DebugCover.class, 0.0, false, 0);

    public static final CoverType[] TYPES = values();
    private static final CoverType[] GLOB_TYPES = new CoverType[256];

    private final int id;
    private final Function<LatitudinalZone, Biome> biomeProvider;
    private final Biome defaultBiome;
    private final double scatterRange;
    private final boolean scatterTo;
    private final int color;

    private final Constructor<? extends CoverGenerator> generatorConstructor;

    CoverType(int globId, Function<LatitudinalZone, Biome> biome, Class<? extends CoverGenerator> generator, double scatterRange, boolean scatterTo, int color) {
        this.id = globId;
        this.biomeProvider = biome;
        this.defaultBiome = biome.apply(LatitudinalZone.TEMPERATE);
        this.scatterRange = scatterRange;
        this.scatterTo = scatterTo;
        this.color = color;

        Constructor<? extends CoverGenerator> constructor = null;

        try {
            constructor = generator.getDeclaredConstructor(CoverType.class);
        } catch (ReflectiveOperationException e) {
            Terrarium.LOGGER.trace("Cover generator {} did not have constructor with CoverType, testing for empty", generator);
        }

        if (constructor == null) {
            try {
                constructor = generator.getDeclaredConstructor();
            } catch (ReflectiveOperationException e) {
                Terrarium.LOGGER.error("Found no default constructor for generator {}", generator, e);
            }
        }

        this.generatorConstructor = constructor;
    }

    CoverType(int id, Biome biome, Class<? extends CoverGenerator> generator, int color) {
        this(id, zone -> biome, generator, 1.0, true, color);
    }

    CoverType(int id, Function<LatitudinalZone, Biome> biome, Class<? extends CoverGenerator> generator, int color) {
        this(id, biome, generator, 1.0, true, color);
    }

    public int getId() {
        return this.id;
    }

    public Biome getDefaultBiome() {
        return this.defaultBiome;
    }

    public Biome getBiome(LatitudinalZone zone) {
        return this.biomeProvider.apply(zone);
    }

    public CoverGenerator createGenerator() {
        try {
            if (this.generatorConstructor.getParameterCount() == 1) {
                return this.generatorConstructor.newInstance(this);
            } else {
                return this.generatorConstructor.newInstance();
            }
        } catch (Exception e) {
            return new BareCover(this);
        }
    }

    public double getScatterRange() {
        return this.scatterRange;
    }

    public boolean canScatterTo() {
        return this.scatterTo;
    }

    public int getColor() {
        return this.color;
    }

    static {
        for (CoverType type : CoverType.values()) {
            if (type.id >= 0) {
                GLOB_TYPES[type.id] = type;
            }
        }
    }

    public static CoverType getGlob(int id) {
        if (id >= 0 && id < 256) {
            CoverType type = GLOB_TYPES[id];
            return type != null ? type : CoverType.NO_DATA;
        }
        return CoverType.NO_DATA;
    }
}
