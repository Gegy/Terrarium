package net.gegy1000.terrarium.server.world.composer.biome;

import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;

public interface BiomeComposer {
    Biome[] composeBiomes(TerrariumWorld terrarium, ColumnData data, ChunkPos columnPos);

    final class Default implements BiomeComposer {
        public static final BiomeComposer INSTANCE = new Default();

        private Default() {
        }

        @Override
        public Biome[] composeBiomes(TerrariumWorld terrarium, ColumnData data, ChunkPos columnPos) {
            return ArrayUtils.fill(new Biome[16 * 16], Biomes.DEFAULT);
        }
    }
}
