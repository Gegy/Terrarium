package dev.gegy.terrarium;

import dev.gegy.terrarium.world.generator.chunk.TerrariumChunkGenerators;

public class Terrarium {
    public static final String ID = "terrarium";

    public static void bootstrap() {
        TerrariumChunkGenerators.bootstrap();
    }
}
