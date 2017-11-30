package net.gegy1000.terrarium.server.map.source

import net.gegy1000.terrarium.server.world.EarthGenerationSettings

interface DataSource {
    val settings: EarthGenerationSettings
}
