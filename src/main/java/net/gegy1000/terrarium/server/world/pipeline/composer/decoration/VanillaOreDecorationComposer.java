package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Method;
import java.util.Random;

public class VanillaOreDecorationComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 6951385192802936659L;

    private static Method generateOres;

    static {
        try {
            generateOres = ReflectionHelper.findMethod(BiomeDecorator.class, "generateOres", "func_76797_b", World.class, Random.class);
        } catch (ReflectionHelper.UnableToFindMethodException e) {
            Terrarium.LOGGER.error("Failed to find generateOres method in BiomeDecorator", e);
        }
    }

    @Override
    public void composeDecoration(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPopulationWriter writer) {
        // TODO: Move to CubicGlue
//        if (generateOres != null) {
//            try {
//                EarthDecorationEventHandler.allowOreGeneration = true;
//                generateOres.invoke(biome.decorator, world, this.horizontalRandom);
//                EarthDecorationEventHandler.allowOreGeneration = false;
//            } catch (IllegalAccessException | InvocationTargetException e) {
//                Terrarium.LOGGER.error("Failed to invoke ore generator", e);
//            }
//        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }
}
