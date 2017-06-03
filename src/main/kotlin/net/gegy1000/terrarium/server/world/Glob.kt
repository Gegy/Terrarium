package net.gegy1000.terrarium.server.world

import net.minecraft.block.BlockDirt
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Biomes
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import java.util.*

enum class Glob(
        val biome: Biome,
        val scatterScale: Double = 1.0
) {
    //Broadleaf - Oak
    //Needleleaf - Taiga
    IRRIGATED_CROPS(Biomes.PLAINS),
    RAINFED_CROPS(Biomes.PLAINS),
    CROPLAND_WITH_VEGETATION(Biomes.PLAINS),
    VEGETATION_WITH_CROPLAND(Biomes.PLAINS),
    BROADLEAF_EVERGREEN(Biomes.FOREST),
    CLOSED_BROADLEAF_DECIDUOUS(Biomes.FOREST),
    OPEN_BROADLEAF_DECIDUOUS(Biomes.FOREST),
    CLOSED_NEEDLELEAF_EVERGREEN(Biomes.FOREST),
    OPEN_NEEDLELEAF(Biomes.FOREST),
    MIXED_BROAD_NEEDLELEAF(Biomes.FOREST),
    FOREST_SHRUBLAND_WITH_GRASS(Biomes.FOREST),
    GRASS_WITH_FOREST_SHRUBLAND(Biomes.PLAINS),
    SHRUBLAND(Biomes.DESERT) {
        val dirt: IBlockState = Blocks.DIRT.defaultState.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)
        val sand: IBlockState = Blocks.SAND.defaultState

        override fun getTopBlock(noise: Double, rand: Random) = if (noise + rand.nextGaussian() * 0.2 > 0.0) this.dirt else this.sand

        override fun getFillerBlock(noise: Double, rand: Random) = this.dirt
    },
    GRASSLAND(Biomes.PLAINS),
    SPARSE_VEGETATION(Biomes.DESERT),
    FRESH_FLOODED_FOREST(Biomes.SWAMPLAND),
    SALINE_FLOODED_FOREST(Biomes.SWAMPLAND),
    FLOODED_GRASSLAND(Biomes.SWAMPLAND),
    URBAN(Biomes.PLAINS),
    BARE(Biomes.EXTREME_HILLS) {
        val dirt: IBlockState = Blocks.DIRT.defaultState.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)
        val stone: IBlockState = Blocks.COBBLESTONE.defaultState
        val sandstone: IBlockState = Blocks.SANDSTONE.defaultState

        override fun getTopBlock(noise: Double, rand: Random) = if (noise > 0.1) this.dirt else if (noise < -0.3) this.sandstone else this.stone

        override fun getFillerBlock(noise: Double, rand: Random) = this.dirt
    },
    WATER(Biomes.OCEAN, 0.5) {
        val water: IBlockState = Blocks.WATER.defaultState
        val sand: IBlockState = Blocks.SAND.defaultState
        val gravel: IBlockState = Blocks.GRAVEL.defaultState

        override fun getTopBlock(noise: Double, rand: Random) = this.water

        override fun getFillerBlock(noise: Double, rand: Random) = if (noise + rand.nextGaussian() * 0.1 > 0.0) this.sand else this.gravel
    },
    SNOW(Biomes.ICE_PLAINS) {
        val snow: IBlockState = Blocks.SNOW.defaultState

        override fun getTopBlock(noise: Double, rand: Random) = this.snow

        override fun getFillerBlock(noise: Double, rand: Random) = this.snow
    },
    NO_DATA(Biomes.PLAINS);

    open fun getTopBlock(noise: Double, rand: Random): IBlockState = this.biome.topBlock

    open fun getFillerBlock(noise: Double, rand: Random): IBlockState = this.biome.fillerBlock

    open fun decorate(world: World, rand: Random, pos: BlockPos) {
    }

    companion object {
        operator fun get(i: Int): Glob {
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
