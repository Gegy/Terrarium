package net.gegy1000.terrarium.server.map.cover;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.generator.BareCover;
import net.gegy1000.terrarium.server.map.cover.generator.BroadleafEvergreenCover;
import net.gegy1000.terrarium.server.map.cover.generator.ClosedBroadleafDeciduousCover;
import net.gegy1000.terrarium.server.map.cover.generator.ClosedNeedleleafEvergreenCover;
import net.gegy1000.terrarium.server.map.cover.generator.CroplandWithVegetationCover;
import net.gegy1000.terrarium.server.map.cover.generator.DebugCover;
import net.gegy1000.terrarium.server.map.cover.generator.FloodedGrasslandCover;
import net.gegy1000.terrarium.server.map.cover.generator.ForestShrublandWithGrassCover;
import net.gegy1000.terrarium.server.map.cover.generator.FreshFloodedForestCover;
import net.gegy1000.terrarium.server.map.cover.generator.GrassWithForestShrublandCover;
import net.gegy1000.terrarium.server.map.cover.generator.GrasslandCover;
import net.gegy1000.terrarium.server.map.cover.generator.IrrigatedCropsCover;
import net.gegy1000.terrarium.server.map.cover.generator.MixedBroadNeedleleafCover;
import net.gegy1000.terrarium.server.map.cover.generator.OpenBroadleafDeciduousCover;
import net.gegy1000.terrarium.server.map.cover.generator.OpenNeedleleafCover;
import net.gegy1000.terrarium.server.map.cover.generator.RainfedCropsCover;
import net.gegy1000.terrarium.server.map.cover.generator.SalineFloodedForestCover;
import net.gegy1000.terrarium.server.map.cover.generator.ShrublandCover;
import net.gegy1000.terrarium.server.map.cover.generator.SnowCover;
import net.gegy1000.terrarium.server.map.cover.generator.SparseVegetationCover;
import net.gegy1000.terrarium.server.map.cover.generator.UrbanCover;
import net.gegy1000.terrarium.server.map.cover.generator.VegetationWithCroplandCover;
import net.gegy1000.terrarium.server.map.cover.generator.WaterCover;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

import java.lang.reflect.Constructor;
import java.util.function.Function;

public enum CoverType {
    IRRIGATED_CROPS(11, CoverBiomeSelectors.BROADLEAF_FOREST_SELECTOR, IrrigatedCropsCover.class),
    RAINFED_CROPS(14, CoverBiomeSelectors.BROADLEAF_FOREST_SELECTOR, RainfedCropsCover.class),
    CROPLAND_WITH_VEGETATION(20, CoverBiomeSelectors.FOREST_SHRUBLAND_SELECTOR, CroplandWithVegetationCover.class),
    VEGETATION_WITH_CROPLAND(30, CoverBiomeSelectors.FOREST_SHRUBLAND_SELECTOR, VegetationWithCroplandCover.class),
    BROADLEAF_EVERGREEN(40, CoverBiomeSelectors.BROADLEAF_FOREST_SELECTOR, BroadleafEvergreenCover.class),
    CLOSED_BROADLEAF_DECIDUOUS(50, CoverBiomeSelectors.BROADLEAF_FOREST_SELECTOR, ClosedBroadleafDeciduousCover.class),
    OPEN_BROADLEAF_DECIDUOUS(60, CoverBiomeSelectors.BROADLEAF_FOREST_SELECTOR, OpenBroadleafDeciduousCover.class),
    CLOSED_NEEDLELEAF_EVERGREEN(70, CoverBiomeSelectors.NEEDLELEAF_FOREST_SELECTOR, ClosedNeedleleafEvergreenCover.class),
    OPEN_NEEDLELEAF(90, CoverBiomeSelectors.NEEDLELEAF_FOREST_SELECTOR, OpenNeedleleafCover.class),
    MIXED_BROAD_NEEDLELEAF(100, Biomes.FOREST, MixedBroadNeedleleafCover.class),
    FOREST_SHRUBLAND_WITH_GRASS(110, CoverBiomeSelectors.FOREST_SHRUBLAND_SELECTOR, ForestShrublandWithGrassCover.class),
    GRASS_WITH_FOREST_SHRUBLAND(120, CoverBiomeSelectors.FOREST_SHRUBLAND_SELECTOR, GrassWithForestShrublandCover.class),
    SHRUBLAND(130, CoverBiomeSelectors.SHRUBLAND_SELECTOR, ShrublandCover.class),
    GRASSLAND(140, CoverBiomeSelectors.GRASSLAND_SELECTOR, GrasslandCover.class),
    SPARSE_VEGETATION(150, CoverBiomeSelectors.GRASSLAND_SELECTOR, SparseVegetationCover.class),
    FRESH_FLOODED_FOREST(160, CoverBiomeSelectors.FLOODED_SELECTOR, FreshFloodedForestCover.class),
    SALINE_FLOODED_FOREST(170, CoverBiomeSelectors.SALINE_FLOODED_SELECTOR, SalineFloodedForestCover.class),
    FLOODED_GRASSLAND(180, CoverBiomeSelectors.FLOODED_SELECTOR, FloodedGrasslandCover.class),
    URBAN(190, Biomes.PLAINS, UrbanCover.class),
    BARE(200, Biomes.DESERT, BareCover.class),
    WATER(210, zone -> Biomes.OCEAN, WaterCover.class, 0.0, false),
    SNOW(220, Biomes.ICE_PLAINS, SnowCover.class),
    NO_DATA(0, Biomes.PLAINS, BareCover.class),
    PROCESSING(-1, Biomes.DEFAULT, BareCover.class),
    DEBUG(-1, zone -> Biomes.DEFAULT, DebugCover.class, 0.0, false);

    public static final CoverType[] TYPES = values();
    private static final CoverType[] GLOB_TYPES = new CoverType[256];

    private final int id;
    private final Function<LatitudinalZone, Biome> biomeProvider;
    private final Biome defaultBiome;
    private final double scatterRange;
    private final boolean scatterTo;

    private final Constructor<? extends CoverGenerator> generatorConstructor;

    CoverType(int globId, Function<LatitudinalZone, Biome> biome, Class<? extends CoverGenerator> generator, double scatterRange, boolean scatterTo) {
        this.id = globId;
        this.biomeProvider = biome;
        this.defaultBiome = biome.apply(LatitudinalZone.TEMPERATE);
        this.scatterRange = scatterRange;
        this.scatterTo = scatterTo;

        Constructor<? extends CoverGenerator> constructor = null;

        try {
            constructor = generator.getDeclaredConstructor(CoverType.class);
        } catch (ReflectiveOperationException e) {
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

    CoverType(int id, Biome biome, Class<? extends CoverGenerator> generator) {
        this(id, zone -> biome, generator, 1.0, true);
    }

    CoverType(int id, Function<LatitudinalZone, Biome> biome, Class<? extends CoverGenerator> generator) {
        this(id, biome, generator, 1.0, true);
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
