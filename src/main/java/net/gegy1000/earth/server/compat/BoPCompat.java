package net.gegy1000.earth.server.compat;

import net.gegy1000.terrarium.server.util.BlockStateParser;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.Loader;

import java.util.Optional;

public class BoPCompat {
    private static final String ID = "biomesoplenty";

    public static Optional<IBlockState> loamyGrass() {
        return state("grass#variant=loamy");
    }

    public static Optional<IBlockState> sandyGrass() {
        return state("grass#variant=sandy");
    }

    public static Optional<IBlockState> siltyGrass() {
        return state("grass#variant=silty");
    }

    public static Optional<IBlockState> loamyDirt() {
        return state("dirt#variant=loamy");
    }

    public static Optional<IBlockState> sandyDirt() {
        return state("dirt#variant=sandy");
    }

    public static Optional<IBlockState> siltyDirt() {
        return state("dirt#variant=silty");
    }

    private static Optional<IBlockState> state(String string) {
        if (Loader.isModLoaded(ID)) {
            return BlockStateParser.parseBlockState(ID + ":" + string);
        } else {
            return Optional.empty();
        }
    }
}
