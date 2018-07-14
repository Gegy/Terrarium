package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.earth.server.EarthDecorationEventHandler;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

public class VanillaOreDecorationComposer extends VanillaDecorationComposer {
    private static final long DECORATION_SEED = 6951385192802936659L;

    private static Method generateOres;

    static {
        try {
            generateOres = ReflectionHelper.findMethod(BiomeDecorator.class, "generateOres", "func_76797_b", World.class, Random.class);
        } catch (ReflectionHelper.UnableToFindMethodException e) {
            Terrarium.LOGGER.error("Failed to find generateOres method in BiomeDecorator", e);
        }
    }

    public VanillaOreDecorationComposer(World world) {
        super(world, DECORATION_SEED);
    }

    @Override
    protected void composeDecoration(IChunkGenerator generator, World world, int chunkX, int chunkZ, Biome biome) {
        // TODO: Hook GameRegistry#sortedGeneratorList to optionally disable
        if (generateOres != null) {
            try {
                EarthDecorationEventHandler.allowOreGeneration = true;
                generateOres.invoke(biome.decorator, world, this.random);
                EarthDecorationEventHandler.allowOreGeneration = false;
            } catch (IllegalAccessException | InvocationTargetException e) {
                Terrarium.LOGGER.error("Failed to invoke ore generator", e);
            }
        }
    }
}
