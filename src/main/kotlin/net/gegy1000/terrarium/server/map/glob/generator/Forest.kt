package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobGenerator
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.map.glob.generator.layer.ReplaceRandomLayer
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectionSeedLayer
import net.minecraft.block.BlockDirt
import net.minecraft.block.BlockLeaves
import net.minecraft.block.BlockOldLeaf
import net.minecraft.block.BlockOldLog
import net.minecraft.block.BlockPlanks
import net.minecraft.block.BlockTallGrass
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.world.World
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.gen.feature.WorldGenTaiga1
import net.minecraft.world.gen.feature.WorldGenTaiga2
import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom
import java.util.Random

open class Forest(type: GlobType) : GlobGenerator(type) {
    companion object {
        val OAK_LOG = Blocks.LOG.defaultState
        val OAK_LEAF = Blocks.LEAVES.defaultState.withProperty(BlockLeaves.CHECK_DECAY, false)

        val JUNGLE_LOG = Blocks.LOG.defaultState.withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE)
        val JUNGLE_LEAF = Blocks.LEAVES.defaultState.withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, false)

        val BIRCH_LOG = Blocks.LOG.defaultState.withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.BIRCH)
        val BIRCH_LEAF = Blocks.LEAVES.defaultState.withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.BIRCH).withProperty(BlockLeaves.CHECK_DECAY, false)

        val TAIGA_1 = WorldGenTaiga1()
        val TAIGA_2 = WorldGenTaiga2(false)

        private const val LAYER_GRASS = 0
        private const val LAYER_DIRT = 1
        private const val LAYER_PODZOL = 2

        private val GRASS = Blocks.GRASS.defaultState
        private val DIRT = Blocks.DIRT.defaultState.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)
        private val PODZOL = Blocks.DIRT.defaultState.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL)

        private val TALL_GRASS = Blocks.TALLGRASS.defaultState.withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS)
    }

    lateinit var coverSelector: GenLayer

    override fun createLayers(world: World) {
        super.createLayers(world)

        var cover: GenLayer = SelectionSeedLayer(2, 1)
        cover = GenLayerVoronoiZoom(1000, cover)
        cover = ReplaceRandomLayer(replace = Forest.LAYER_DIRT, replacement = Forest.LAYER_PODZOL, chance = 4, seed = 2000, parent = cover)
        cover = GenLayerFuzzyZoom(3000, cover)

        this.coverSelector = cover
        this.coverSelector.initWorldGenSeed(world.seed)
    }

    override fun coverDecorate(globBuffer: Array<GlobType>, heightBuffer: IntArray, primer: ChunkPrimer, random: Random, x: Int, z: Int) {
        this.foreach(globBuffer) { localX: Int, localZ: Int ->
            val bufferIndex = localX + localZ * 16
            if (random.nextInt(4) == 0) {
                val y = heightBuffer[bufferIndex]
                primer.setBlockState(localX, y + 1, localZ, Forest.TALL_GRASS)
            }
        }
    }

    override fun getCover(glob: Array<GlobType>, cover: Array<IBlockState>, x: Int, z: Int, random: Random) {
        val coverLayer = this.sampleChunk(this.coverSelector, x, z)

        this.foreach(glob) { localX: Int, localZ: Int ->
            val index = localX + localZ * 16
            cover[index] = when (coverLayer[index]) {
                Forest.LAYER_GRASS -> Forest.GRASS
                Forest.LAYER_DIRT -> Forest.DIRT
                else -> Forest.PODZOL
            }
        }
    }
}
