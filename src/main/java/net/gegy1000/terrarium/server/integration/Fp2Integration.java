package net.gegy1000.terrarium.server.integration;

import net.daporkchop.fp2.mode.heightmap.HeightmapPos;
import net.daporkchop.fp2.mode.heightmap.HeightmapTile;
import net.daporkchop.fp2.mode.heightmap.event.RegisterRoughHeightmapGeneratorsEvent;
import net.daporkchop.fp2.mode.heightmap.server.gen.rough.AbstractRoughHeightmapGenerator;
import net.gegy1000.justnow.executor.CurrentThreadExecutor;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.RoughHeightmapComposer;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.DataGenerator;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;

public final class Fp2Integration {
    @SubscribeEvent
    public static void onRegisterRoughHeightmapGenerators(RegisterRoughHeightmapGeneratorsEvent event) {
        event.registry().addFirst("terrarium", world -> {
            TerrariumWorld terrarium = TerrariumWorld.get(world);
            if (terrarium == null) {
                return null;
            }

            RoughHeightmapComposer composer = terrarium.getRoughHeightmapComposer();
            if (composer == null) {
                return null;
            }

            DataGenerator dataGenerator = terrarium.getDataGenerator();
            Collection<DataKey<?>> requiredData = composer.getRequiredData();

            return new AbstractRoughHeightmapGenerator(world) {
                @Override
                public boolean supportsLowResolution() {
                    return true;
                }

                @Override
                public void generate(HeightmapPos pos, HeightmapTile tile) {
                    int level = pos.level();
                    int size = 16 << level;

                    DataView view = DataView.square(pos.blockX(), pos.blockZ(), size);
                    ColumnData data = CurrentThreadExecutor.blockOn(dataGenerator.generate(view, requiredData));

                    composer.compose(terrarium, data, pos, tile);
                }
            };
        });
    }
}
