package net.gegy1000.terrarium.server.capability

import net.gegy1000.terrarium.server.map.source.GeocodingSource
import net.gegy1000.terrarium.server.map.source.glob.GlobSource
import net.gegy1000.terrarium.server.map.source.height.HeightSource
import net.gegy1000.terrarium.server.map.source.osm.OverpassSource
import net.gegy1000.terrarium.server.map.source.tiled.TiledSource
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.gegy1000.terrarium.server.world.generator.EarthGenerationHandler
import net.minecraft.util.EnumFacing
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider

interface TerrariumWorldData : ICapabilityProvider {
    val generationHandler: EarthGenerationHandler

    val heightSource: HeightSource
    val globSource: GlobSource
    val overpassSource: OverpassSource
    val geocodingSource: GeocodingSource

    val tiledBufferSources: Set<TiledSource<*>>

    class Implementation(world: World) : TerrariumWorldData {
        private val settings = EarthGenerationSettings.deserialize(world.worldInfo.generatorOptions)

        override val generationHandler: EarthGenerationHandler

        override val heightSource = HeightSource(settings)
        override val globSource = GlobSource(settings)
        override val overpassSource = OverpassSource(settings)
        override val geocodingSource = GeocodingSource(settings)

        override val tiledBufferSources: Set<TiledSource<*>> = setOf(heightSource, globSource, overpassSource)

        init {
            generationHandler = EarthGenerationHandler(this, settings, world.height - 1)

            overpassSource.loadQuery()
        }

        override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
            return capability == TerrariumCapabilities.worldDataCapability
        }

        override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
            if (this.hasCapability(capability, facing)) {
                return TerrariumCapabilities.worldDataCapability.cast(this)
            }
            return null
        }
    }
}
