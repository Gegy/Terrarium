package net.gegy1000.terrarium.server.world;

import io.github.opencubicchunks.cubicchunks.api.util.IntRange;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorldType;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import net.gegy1000.terrarium.server.world.chunk.ComposableCubeGenerator;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;

public class TerrariumCubeWorldType extends TerrariumWorldType implements ICubicWorldType {
    public TerrariumCubeWorldType(TerrariumWorldDefinition worldType) {
        super(worldType);
    }

    @Nullable
    @Override
    public final ICubeGenerator createCubeGenerator(World world) {
        return new ComposableCubeGenerator(world);
    }

    @Override
    public IntRange calculateGenerationHeightRange(WorldServer world) {
        return new IntRange(0, Short.MAX_VALUE);
    }

    @Override
    public boolean hasCubicGeneratorForWorld(World object) {
        return true;
    }
}
