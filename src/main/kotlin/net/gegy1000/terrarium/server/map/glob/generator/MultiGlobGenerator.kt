package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobGenerator
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectWeightedLayer
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.world.World
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.gen.layer.GenLayer
import java.util.Random
import kotlin.reflect.full.createInstance

abstract class MultiGlobGenerator(type: GlobType, vararg val entries: Entry) : GlobGenerator(type) {
    val generatorMap = HashMap<Int, GlobGenerator>()

    lateinit var globSelector: GenLayer

    override fun initialize(world: World, globBuffer: Array<GlobType>, heightBuffer: IntArray, coverBuffer: Array<IBlockState>, fillerBuffer: Array<IBlockState>) {
        super.initialize(world, globBuffer, heightBuffer, coverBuffer, fillerBuffer)

        for ((type) in this.entries) {
            val generator = type.generator.createInstance()
            this.generatorMap.put(type.id, generator)

            generator.initialize(world, Array(256, { GlobType.NO_DATA }), heightBuffer, Array(256, { Blocks.STONE.defaultState }), Array(256, { Blocks.STONE.defaultState }))
        }
    }

    override fun createLayers() {
        val layerEntries = this.entries.map { SelectWeightedLayer.Entry(it.type.id, it.weight) }.toTypedArray()

        this.globSelector = this.zoom(SelectWeightedLayer(1, *layerEntries))
        this.globSelector.initWorldGenSeed(this.seed)
    }

    override fun decorate(random: Random, x: Int, z: Int) {
        val sampledTypes = this.sampleChunk(this.globSelector, x, z)
        val sampledType = this.generatorMap[sampledTypes[136]]

        sampledType?.decorate(random, x, z)
    }

    override fun coverDecorate(primer: ChunkPrimer, random: Random, x: Int, z: Int) {
        val sampledTypes = this.sampleChunk(this.globSelector, x, z)

        for ((id, generator) in this.generatorMap) {
            for ((index, glob) in this.globBuffer.withIndex()) {
                generator.globBuffer[index] = if (glob == this.type) generator.type else GlobType.NO_DATA
            }
            generator.coverDecorate(FilterPrimer(primer, sampledTypes, id), random, x, z)
        }
    }

    override fun getCover(x: Int, z: Int, random: Random) {
        val sampledTypes = this.sampleChunk(this.globSelector, x, z)

        for (generator in this.generatorMap.values) {
            for ((index, glob) in this.globBuffer.withIndex()) {
                generator.globBuffer[index] = if (glob == this.type) generator.type else GlobType.NO_DATA
            }
            generator.getCover(x, z, random)
        }

        this.iterate { localX: Int, localZ: Int ->
            val index = localX + localZ * 16
            val sampledType = this.generatorMap[sampledTypes[index]]

            this.coverBuffer[index] = sampledType!!.coverBuffer[index]
        }
    }

    override fun getFiller(x: Int, z: Int, random: Random) {
        val sampledTypes = this.sampleChunk(this.globSelector, x, z)

        for (generator in this.generatorMap.values) {
            for ((index, glob) in this.globBuffer.withIndex()) {
                generator.globBuffer[index] = if (glob == this.type) generator.type else GlobType.NO_DATA
            }
            generator.getFiller(x, z, random)
        }

        this.iterate { localX: Int, localZ: Int ->
            val index = localX + localZ * 16
            val sampledType = this.generatorMap[sampledTypes[index]]

            this.fillerBuffer[index] = sampledType!!.fillerBuffer[index]
        }
    }

    abstract fun zoom(layer: GenLayer): GenLayer

    data class Entry(val type: GlobType, val weight: Int)

    private class FilterPrimer(val parent: ChunkPrimer, val sampledTypes: IntArray, val type: Int) : ChunkPrimer() {
        override fun setBlockState(x: Int, y: Int, z: Int, state: IBlockState?) {
            if (this.sampledTypes[x + z * 16] == this.type) {
                this.parent.setBlockState(x, y, z, state)
            }
        }

        override fun getBlockState(x: Int, y: Int, z: Int) = this.parent.getBlockState(x, y, z)
    }
}
