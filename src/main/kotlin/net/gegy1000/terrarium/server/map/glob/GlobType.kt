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

enum class GlobType(val id: Int, val biome: Biome, val generator: KClass<out GlobGenerator>) {
    IRRIGATED_CROPS(11, Biomes.FOREST, IrrigatedCrops::class),
    RAINFED_CROPS(14, Biomes.PLAINS, RainfedCrops::class),
    CROPLAND_WITH_VEGETATION(20, Biomes.PLAINS, CroplandWithVegetation::class),
    VEGETATION_WITH_CROPLAND(30, Biomes.PLAINS, VegetationWithCropland::class),
    BROADLEAF_EVERGREEN(40, Biomes.FOREST, BroadleafEvergreen::class),
    CLOSED_BROADLEAF_DECIDUOUS(50, Biomes.FOREST, ClosedBroadleafDeciduous::class),
    OPEN_BROADLEAF_DECIDUOUS(60, Biomes.FOREST, OpenBroadleafDeciduous::class),
    CLOSED_NEEDLELEAF_EVERGREEN(70, Biomes.FOREST, ClosedNeedleleafEvergreen::class),
    OPEN_NEEDLELEAF(90, Biomes.FOREST, OpenNeedleleaf::class),
    MIXED_BROAD_NEEDLELEAF(100, Biomes.FOREST, MixedBroadNeedleleaf::class),
    FOREST_SHRUBLAND_WITH_GRASS(110, Biomes.PLAINS, ForestShrublandWithGrass::class),
    GRASS_WITH_FOREST_SHRUBLAND(120, Biomes.PLAINS, GrassWithForestShrubland::class),
    SHRUBLAND(130, Biomes.DESERT, Shrubland::class),
    GRASSLAND(140, Biomes.SAVANNA, Grassland::class),
    SPARSE_VEGETATION(150, Biomes.DESERT, SparseVegetation::class),
    FRESH_FLOODED_FOREST(160, Biomes.SWAMPLAND, FreshFloodedForest::class),
    SALINE_FLOODED_FOREST(170, Biomes.SWAMPLAND, SalineFloodedForest::class),
    FLOODED_GRASSLAND(180, Biomes.SWAMPLAND, FloodedGrassland::class),
    URBAN(190, Biomes.PLAINS, Urban::class),
    BARE(200, Biomes.DESERT, Bare::class),
    WATER(210, Biomes.OCEAN, Water::class),
    SNOW(220, Biomes.ICE_PLAINS, Snow::class),
    NO_DATA(0, Biomes.PLAINS, Bare::class);

    companion object {
        val TYPES = Array(256, { GlobType.NO_DATA })

        init {
            for (type in GlobType.values()) {
                TYPES[type.id] = type
            }
        }

        operator fun get(id: Int): GlobType {
            if (id in 0..255) {
                return TYPES[id]
            }
            return NO_DATA
        }
    }
}
