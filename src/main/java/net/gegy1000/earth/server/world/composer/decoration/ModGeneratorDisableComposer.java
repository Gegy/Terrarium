package net.gegy1000.earth.server.world.composer.decoration;

import dev.gegy.gengen.api.CubicPos;
import dev.gegy.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.earth.server.world.compatibility.hooks.ModGenerators;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;

public final class ModGeneratorDisableComposer implements DecorationComposer {
    @Override
    public void composeDecoration(TerrariumWorld terrarium, CubicPos pos, ChunkPopulationWriter writer) {
        // hack: when populating as a column, Forge calls the modded generators to run.
        //       we are manually generating modded generators, so we want to cancel this by the time it is called.
        ModGenerators.getAndHookSortedGenerators();
    }
}
