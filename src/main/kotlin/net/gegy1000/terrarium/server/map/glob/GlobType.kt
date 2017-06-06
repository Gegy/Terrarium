package net.gegy1000.terrarium.server.map.glob

import net.gegy1000.terrarium.server.map.glob.generator.Bare
import net.gegy1000.terrarium.server.map.glob.generator.BroadleafEvergreen
import net.gegy1000.terrarium.server.map.glob.generator.ClosedBroadleafDeciduous
import net.gegy1000.terrarium.server.map.glob.generator.ClosedNeedleleafEvergreen
import net.gegy1000.terrarium.server.map.glob.generator.CroplandWithVegetation
import net.gegy1000.terrarium.server.map.glob.generator.FloodedGrassland
import net.gegy1000.terrarium.server.map.glob.generator.ForestShrublandWithGrass
import net.gegy1000.terrarium.server.map.glob.generator.FreshFloodedForest
import net.gegy1000.terrarium.server.map.glob.generator.GrassWithForestShrubland
import net.gegy1000.terrarium.server.map.glob.generator.Grassland
import net.gegy1000.terrarium.server.map.glob.generator.IrrigatedCrops
import net.gegy1000.terrarium.server.map.glob.generator.MixedBroadNeedleleaf
import net.gegy1000.terrarium.server.map.glob.generator.OpenBroadleafDeciduous
import net.gegy1000.terrarium.server.map.glob.generator.OpenNeedleleaf
import net.gegy1000.terrarium.server.map.glob.generator.RainfedCrops
import net.gegy1000.terrarium.server.map.glob.generator.SalineFloodedForest
import net.gegy1000.terrarium.server.map.glob.generator.Shrubland
import net.gegy1000.terrarium.server.map.glob.generator.Snow
import net.gegy1000.terrarium.server.map.glob.generator.SparseVegetation
import net.gegy1000.terrarium.server.map.glob.generator.Urban
import net.gegy1000.terrarium.server.map.glob.generator.VegetationWithCropland
import net.gegy1000.terrarium.server.map.glob.generator.Water
import net.minecraft.init.Biomes
import net.minecraft.world.biome.Biome
import kotlin.reflect.KClass

enum class GlobType(val biome: Biome, val generator: KClass<out GlobGenerator>) {
    IRRIGATED_CROPS(Biomes.FOREST, IrrigatedCrops::class),
    RAINFED_CROPS(Biomes.PLAINS, RainfedCrops::class),
    CROPLAND_WITH_VEGETATION(Biomes.PLAINS, CroplandWithVegetation::class),
    VEGETATION_WITH_CROPLAND(Biomes.PLAINS, VegetationWithCropland::class),
    BROADLEAF_EVERGREEN(Biomes.FOREST, BroadleafEvergreen::class),
    CLOSED_BROADLEAF_DECIDUOUS(Biomes.FOREST, ClosedBroadleafDeciduous::class),
    OPEN_BROADLEAF_DECIDUOUS(Biomes.FOREST, OpenBroadleafDeciduous::class),
    CLOSED_NEEDLELEAF_EVERGREEN(Biomes.FOREST, ClosedNeedleleafEvergreen::class),
    OPEN_NEEDLELEAF(Biomes.FOREST, OpenNeedleleaf::class),
    MIXED_BROAD_NEEDLELEAF(Biomes.FOREST, MixedBroadNeedleleaf::class),
    FOREST_SHRUBLAND_WITH_GRASS(Biomes.PLAINS, ForestShrublandWithGrass::class),
    GRASS_WITH_FOREST_SHRUBLAND(Biomes.PLAINS, GrassWithForestShrubland::class),
    SHRUBLAND(Biomes.DESERT, Shrubland::class),
    GRASSLAND(Biomes.SAVANNA, Grassland::class),
    SPARSE_VEGETATION(Biomes.DESERT, SparseVegetation::class),
    FRESH_FLOODED_FOREST(Biomes.SWAMPLAND, FreshFloodedForest::class),
    SALINE_FLOODED_FOREST(Biomes.SWAMPLAND, SalineFloodedForest::class),
    FLOODED_GRASSLAND(Biomes.SWAMPLAND, FloodedGrassland::class),
    URBAN(Biomes.PLAINS, Urban::class),
    BARE(Biomes.DESERT, Bare::class),
    WATER(Biomes.OCEAN, Water::class),
    SNOW(Biomes.ICE_PLAINS, Snow::class),
    NO_DATA(Biomes.PLAINS, Bare::class);

    companion object {
        operator fun get(i: Int): GlobType {
            return when (i) {
                11 -> IRRIGATED_CROPS
                14 -> RAINFED_CROPS
                20 -> CROPLAND_WITH_VEGETATION
                30 -> VEGETATION_WITH_CROPLAND
                40 -> BROADLEAF_EVERGREEN
                50 -> CLOSED_BROADLEAF_DECIDUOUS
                60 -> OPEN_BROADLEAF_DECIDUOUS
                70 -> CLOSED_NEEDLELEAF_EVERGREEN
                90 -> OPEN_NEEDLELEAF
                100 -> MIXED_BROAD_NEEDLELEAF
                110 -> FOREST_SHRUBLAND_WITH_GRASS
                120 -> GRASS_WITH_FOREST_SHRUBLAND
                130 -> SHRUBLAND
                140 -> GRASSLAND
                150 -> SPARSE_VEGETATION
                160 -> FRESH_FLOODED_FOREST
                170 -> SALINE_FLOODED_FOREST
                180 -> FLOODED_GRASSLAND
                190 -> URBAN
                200 -> BARE
                210 -> WATER
                220 -> SNOW
                else -> NO_DATA
            }
        }
    }
}
