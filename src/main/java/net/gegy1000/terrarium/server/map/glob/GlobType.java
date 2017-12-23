package net.gegy1000.terrarium.server.map.glob;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.map.glob.generator.Bare;
import net.gegy1000.terrarium.server.map.glob.generator.BroadleafEvergreen;
import net.gegy1000.terrarium.server.map.glob.generator.ClosedBroadleafDeciduous;
import net.gegy1000.terrarium.server.map.glob.generator.ClosedNeedleleafEvergreen;
import net.gegy1000.terrarium.server.map.glob.generator.CroplandWithVegetation;
import net.gegy1000.terrarium.server.map.glob.generator.FloodedGrassland;
import net.gegy1000.terrarium.server.map.glob.generator.ForestShrublandWithGrass;
import net.gegy1000.terrarium.server.map.glob.generator.FreshFloodedForest;
import net.gegy1000.terrarium.server.map.glob.generator.GrassWithForestShrubland;
import net.gegy1000.terrarium.server.map.glob.generator.Grassland;
import net.gegy1000.terrarium.server.map.glob.generator.IrrigatedCrops;
import net.gegy1000.terrarium.server.map.glob.generator.MixedBroadNeedleleaf;
import net.gegy1000.terrarium.server.map.glob.generator.OpenBroadleafDeciduous;
import net.gegy1000.terrarium.server.map.glob.generator.OpenNeedleleaf;
import net.gegy1000.terrarium.server.map.glob.generator.RainfedCrops;
import net.gegy1000.terrarium.server.map.glob.generator.SalineFloodedForest;
import net.gegy1000.terrarium.server.map.glob.generator.Shrubland;
import net.gegy1000.terrarium.server.map.glob.generator.Snow;
import net.gegy1000.terrarium.server.map.glob.generator.SparseVegetation;
import net.gegy1000.terrarium.server.map.glob.generator.Urban;
import net.gegy1000.terrarium.server.map.glob.generator.VegetationWithCropland;
import net.gegy1000.terrarium.server.map.glob.generator.Water;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

import java.lang.reflect.Constructor;

public enum GlobType {
    IRRIGATED_CROPS(11, Biomes.FOREST, IrrigatedCrops.class),
    RAINFED_CROPS(14, Biomes.PLAINS, RainfedCrops.class),
    CROPLAND_WITH_VEGETATION(20, Biomes.PLAINS, CroplandWithVegetation.class),
    VEGETATION_WITH_CROPLAND(30, Biomes.PLAINS, VegetationWithCropland.class),
    BROADLEAF_EVERGREEN(40, Biomes.FOREST, BroadleafEvergreen.class),
    CLOSED_BROADLEAF_DECIDUOUS(50, Biomes.FOREST, ClosedBroadleafDeciduous.class),
    OPEN_BROADLEAF_DECIDUOUS(60, Biomes.FOREST, OpenBroadleafDeciduous.class),
    CLOSED_NEEDLELEAF_EVERGREEN(70, Biomes.FOREST, ClosedNeedleleafEvergreen.class),
    OPEN_NEEDLELEAF(90, Biomes.FOREST, OpenNeedleleaf.class),
    MIXED_BROAD_NEEDLELEAF(100, Biomes.FOREST, MixedBroadNeedleleaf.class),
    FOREST_SHRUBLAND_WITH_GRASS(110, Biomes.PLAINS, ForestShrublandWithGrass.class),
    GRASS_WITH_FOREST_SHRUBLAND(120, Biomes.PLAINS, GrassWithForestShrubland.class),
    SHRUBLAND(130, Biomes.DESERT, Shrubland.class),
    GRASSLAND(140, Biomes.SAVANNA, Grassland.class),
    SPARSE_VEGETATION(150, Biomes.DESERT, SparseVegetation.class),
    FRESH_FLOODED_FOREST(160, Biomes.SWAMPLAND, FreshFloodedForest.class),
    SALINE_FLOODED_FOREST(170, Biomes.SWAMPLAND, SalineFloodedForest.class),
    FLOODED_GRASSLAND(180, Biomes.SWAMPLAND, FloodedGrassland.class),
    URBAN(190, Biomes.PLAINS, Urban.class),
    BARE(200, Biomes.DESERT, Bare.class),
    WATER(210, Biomes.OCEAN, Water.class, 0.05, false),
    SNOW(220, Biomes.ICE_PLAINS, Snow.class),
    NO_DATA(0, Biomes.PLAINS, Bare.class);

    private static final GlobType[] TYPES = new GlobType[256];

    private final int id;
    private final Biome biome;
    private final Class<? extends GlobGenerator> generator;
    private final double scatterRange;
    private final boolean scatterTo;

    private final Constructor<? extends GlobGenerator> generatorConstructor;

    GlobType(int id, Biome biome, Class<? extends GlobGenerator> generator, double scatterRange, boolean scatterTo) {
        this.id = id;
        this.biome = biome;
        this.generator = generator;
        this.scatterRange = scatterRange;
        this.scatterTo = scatterTo;

        Constructor<? extends GlobGenerator> constructor = null;
        try {
            constructor = this.generator.getDeclaredConstructor();
        } catch (ReflectiveOperationException e) {
            Terrarium.LOGGER.error("Found no default constructor for generator {}", generator, e);
        }

        this.generatorConstructor = constructor;
    }

    GlobType(int id, Biome biome, Class<? extends GlobGenerator> generator) {
        this(id, biome, generator, 1.0, true);
    }

    public int getId() {
        return this.id;
    }

    public Biome getBiome() {
        return this.biome;
    }

    public GlobGenerator createGenerator() {
        try {
            return this.generatorConstructor.newInstance();
        } catch (Exception e) {
            return new Bare();
        }
    }

    public double getScatterRange() {
        return this.scatterRange;
    }

    public boolean canScatterTo() {
        return this.scatterTo;
    }

    static {
        for (GlobType type : GlobType.values()) {
            TYPES[type.id] = type;
        }
    }

    public static GlobType get(int id) {
        if (id >= 0 && id < 256) {
            GlobType type = TYPES[id];
            return type != null ? type : GlobType.NO_DATA;
        }
        return GlobType.NO_DATA;
    }
}
