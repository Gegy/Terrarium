package dev.gegy.terrarium;

import com.mojang.serialization.Codec;
import dev.gegy.terrarium.backend.util.Util;

public enum Biome {
    // Adapted from: https://github.com/toolbox4minecraft/amidst
    BADLANDS("minecraft:badlands", "Badlands", 0xd94515),
    BAMBOO_JUNGLE("minecraft:bamboo_jungle", "Bamboo Jungle", 0x768e14),
    BEACH("minecraft:beach", "Beach", 0xfade55),
    BIRCH_FOREST("minecraft:birch_forest", "Birch Forest", 0x307444),
    CHERRY_GROVE("minecraft:cherry_grove", "Cherry Grove", 0xf7b9dc),
    COLD_OCEAN("minecraft:cold_ocean", "Cold Ocean", 0x202070),
    DARK_FOREST("minecraft:dark_forest", "Dark Forest", 0x40511a),
    DEEP_COLD_OCEAN("minecraft:deep_cold_ocean", "Deep Cold Ocean", 0x202038),
    DEEP_FROZEN_OCEAN("minecraft:deep_frozen_ocean", "Deep Frozen Ocean", 0x404090),
    DEEP_LUKEWARM_OCEAN("minecraft:deep_lukewarm_ocean", "Deep Lukewarm Ocean", 0x000040),
    DEEP_OCEAN("minecraft:deep_ocean", "Deep Ocean", 0x000030),
    DESERT("minecraft:desert", "Desert", 0xfa9418),
    ERODED_BADLANDS("minecraft:eroded_badlands", "Eroded Badlands", 0xff6d3d),
    FLOWER_FOREST("minecraft:flower_forest", "Flower Forest", 0x2d8e49),
    FOREST("minecraft:forest", "Forest", 0x056621),
    FROZEN_OCEAN("minecraft:frozen_ocean", "Frozen Ocean", 0x7070d6),
    FROZEN_PEAKS("minecraft:frozen_peaks", "Frozen Peaks", 0),
    FROZEN_RIVER("minecraft:frozen_river", "Frozen River", 0xa0a0ff),
    GROVE("minecraft:grove", "Grove", 0),
    ICE_SPIKES("minecraft:ice_spikes", "Ice Spikes", 0xb4dcdc),
    JAGGED_PEAKS("minecraft:jagged_peaks", "Jagged Peaks", 0),
    JUNGLE("minecraft:jungle", "Jungle", 0x537b09),
    LUKEWARM_OCEAN("minecraft:lukewarm_ocean", "Lukewarm Ocean", 0x000090),
    MANGROVE_SWAMP("minecraft:mangrove_swamp", "Mangrove Swamp", 0x5e9510),
    MEADOW("minecraft:meadow", "Meadow", 0),
    MUSHROOM_FIELDS("minecraft:mushroom_fields", "Mushroom Fields", 0xff00ff),
    OCEAN("minecraft:ocean", "Ocean", 0x000070),
    OLD_GROWTH_BIRCH_FOREST("minecraft:old_growth_birch_forest", "Old Growth Birch Forest", 0x589c6c),
    OLD_GROWTH_PINE_TAIGA("minecraft:old_growth_pine_taiga", "Old Growth Pine Taiga", 0x596651),
    OLD_GROWTH_SPRUCE_TAIGA("minecraft:old_growth_spruce_taiga", "Old Growth Spruce Taiga", 0x818e79),
    PLAINS("minecraft:plains", "Plains", 0x8DB360),
    RIVER("minecraft:river", "River", 0x0000ff),
    SAVANNA("minecraft:savanna", "Savanna", 0xbdb25f),
    SAVANNA_PLATEAU("minecraft:savanna_plateau", "Savanna Plateau", 0xa79d64),
    SNOWY_BEACH("minecraft:snowy_beach", "Snowy Beach", 0xfaf0c0),
    SNOWY_PLAINS("minecraft:snowy_plains", "Snowy Plains", 0xffffff),
    SNOWY_SLOPES("minecraft:snowy_slopes", "Snowy Slopes", 0),
    SNOWY_TAIGA("minecraft:snowy_taiga", "Snowy Taiga", 0x31554a),
    SPARSE_JUNGLE("minecraft:sparse_jungle", "Sparse Jungle", 0x628b17),
    STONY_PEAKS("minecraft:stony_peaks", "Stony Peaks", 0),
    STONY_SHORE("minecraft:stony_shore", "Stony Shore", 0xa2a284),
    SUNFLOWER_PLAINS("minecraft:sunflower_plains", "Sunflower Plains", 0),
    SWAMP("minecraft:swamp", "Swamp", 0x07f9b2),
    TAIGA("minecraft:taiga", "Taiga", 0x0b6659),
    WARM_OCEAN("minecraft:warm_ocean", "Warm Ocean", 0x0000ac),
    WINDSWEPT_FOREST("minecraft:windswept_forest", "Windswept Forest", 0x22551c),
    WINDSWEPT_GRAVELLY_HILLS("minecraft:windswept_gravelly_hills", "Windswept Gravelly Hills", 0x789878),
    WINDSWEPT_HILLS("minecraft:windswept_hills", "Windswept Hills", 0x606060),
    WINDSWEPT_SAVANNA("minecraft:windswept_savanna", "Windswept Savanna", 0xe5da87),
    WOODED_BADLANDS("minecraft:wooded_badlands", "Wooded Badlands", 0xb09765),
    ;

    public static final Codec<Biome> CODEC = Util.stringLookupCodec(values(), Biome::id);

    private final String id;
    private final String name;
    private final int color;

    Biome(final String id, final String name, final int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public int color() {
        return color;
    }

    @Override
    public String toString() {
        return name;
    }
}
