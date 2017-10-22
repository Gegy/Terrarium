package net.gegy1000.terrarium.server.world

import com.google.gson.Gson
import net.gegy1000.terrarium.server.world.generator.EarthGenerationHandler

data class EarthGenerationSettings(
        var mapFeatures: Boolean = true,
        var buildings: Boolean = true,
        var streets: Boolean = true,
        var decorate: Boolean = true,
        var terrainHeightScale: Double = 1.0,
        var scale: Double = 0.03,
        val heightOffset: Int = 5,
        val scatterRange: Int = 200
) {
    companion object {
        private val GSON = Gson()

        fun deserialize(settings: String): EarthGenerationSettings {
            if (settings.isEmpty()) {
                return EarthGenerationSettings()
            }
            return GSON.fromJson(settings, EarthGenerationSettings::class.java)
        }
    }

    val finalScale: Double
        get() = scale * EarthGenerationHandler.REAL_SCALE

    val scaledWidth: Double
        get() = finalScale * EarthGenerationHandler.WIDTH
    val scaledHeight: Double
        get() = finalScale * EarthGenerationHandler.HEIGHT

    val scaleRatioX: Double
        get() = 1.0 / (this.scaledWidth - 1) * (EarthGenerationHandler.WIDTH - 1)
    val scaleRatioZ: Double
        get() = 1.0 / (this.scaledHeight - 1) * (EarthGenerationHandler.HEIGHT - 1)

    fun serialize() = GSON.toJson(this)
}
