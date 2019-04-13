package net.gegy1000.terrarium.server.world.cover;

import net.gegy1000.earth.server.world.cover.ClimaticZone;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

import java.util.function.Function;

public class CoverBiomeSelectors {
    public static final Function<ClimaticZone, Biome> BROADLEAF_FOREST_SELECTOR = zone -> {
        switch (zone) {
            case FRIGID:
                return Biomes.COLD_TAIGA;
            case TEMPERATE:
            case SUBTROPICS:
                return Biomes.FOREST;
            case TROPICS:
                return Biomes.JUNGLE;
        }
        return Biomes.FOREST;
    };

    public static final Function<ClimaticZone, Biome> NEEDLELEAF_FOREST_SELECTOR = zone -> {
        switch (zone) {
            case FRIGID:
                return Biomes.COLD_TAIGA;
            case TEMPERATE:
                return Biomes.TAIGA;
            case SUBTROPICS:
            case TROPICS:
                return Biomes.FOREST;
        }
        return Biomes.TAIGA;
    };

    public static final Function<ClimaticZone, Biome> FLOODED_SELECTOR = zone -> {
        switch (zone) {
            case FRIGID:
                return Biomes.TAIGA;
            case SUBTROPICS:
            case TEMPERATE:
                return Biomes.SWAMPLAND;
            case TROPICS:
                return Biomes.JUNGLE;
        }
        return Biomes.SWAMPLAND;
    };

    public static final Function<ClimaticZone, Biome> SALINE_FLOODED_SELECTOR = zone -> {
        switch (zone) {
            case FRIGID:
                return Biomes.TAIGA;
            case SUBTROPICS:
            case TEMPERATE:
                return Biomes.SWAMPLAND;
            case TROPICS:
                return Biomes.FOREST;
        }
        return Biomes.SWAMPLAND;
    };

    public static final Function<ClimaticZone, Biome> FOREST_SHRUBLAND_SELECTOR = zone -> {
        switch (zone) {
            case FRIGID:
                return Biomes.ICE_PLAINS;
            case SUBTROPICS:
            case TEMPERATE:
                return Biomes.PLAINS;
            case TROPICS:
                return Biomes.PLAINS;
        }
        return Biomes.PLAINS;
    };

    public static final Function<ClimaticZone, Biome> SHRUBLAND_SELECTOR = zone -> {
        switch (zone) {
            case FRIGID:
            case TEMPERATE:
                return Biomes.SAVANNA;
            case SUBTROPICS:
            case TROPICS:
                return Biomes.PLAINS;
        }
        return Biomes.SAVANNA;
    };

    public static final Function<ClimaticZone, Biome> GRASSLAND_SELECTOR = zone -> {
        switch (zone) {
            case FRIGID:
            case TEMPERATE:
                return Biomes.SAVANNA;
            case SUBTROPICS:
            case TROPICS:
                return Biomes.PLAINS;
        }
        return Biomes.SAVANNA;
    };
}
