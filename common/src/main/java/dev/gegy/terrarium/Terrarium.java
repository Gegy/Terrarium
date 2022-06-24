package dev.gegy.terrarium;

import dev.gegy.terrarium.world.generator.chunk.TerrariumChunkGenerators;
import dev.gegy.terrarium.world.generator.chunk.data.GeoChunkLoader;

public class Terrarium {
    public static final String ID = "terrarium";

    public static void bootstrap() {
        TerrariumChunkGenerators.bootstrap();
        GeoChunkLoader.bootstrap();
    }
}
