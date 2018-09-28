package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.cubicglue.util.PseudoRandomMap;
import net.gegy1000.cubicglue.util.wrapper.BiomeDecorationWorld;
import net.gegy1000.earth.server.EarthDecorationEventHandler;
import net.gegy1000.earth.server.world.CubicGenerationFormat;
import net.gegy1000.earth.server.world.FeatureGenerationFormat;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

public class VanillaOreDecorationComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 6951385192802936659L;

    private final PseudoRandomMap randomMap;
    private final Random horizontalRandom = new Random(0);

    private static Method generateOres;

    static {
        try {
            generateOres = ReflectionHelper.findMethod(BiomeDecorator.class, "generateOres", "func_76797_b", World.class, Random.class);
        } catch (ReflectionHelper.UnableToFindMethodException e) {
            Terrarium.LOGGER.error("Failed to find generateOres method in BiomeDecorator", e);
        }
    }

    public VanillaOreDecorationComposer(World world, CubicGenerationFormat format) {
        this.randomMap = new PseudoRandomMap(world, DECORATION_SEED);
    }

    @Override
    public void composeDecoration(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPopulationWriter writer) {
        if (generateOres != null) {
            this.randomMap.initPosSeed(pos.getMinX(), pos.getMinZ());
            this.horizontalRandom.setSeed(this.randomMap.next());

            BiomeDecorationWorld wrappedWorld = new BiomeDecorationWorld(writer.getGlobal(), pos);
            Biome biome = writer.getCenterBiome();

            try {
                // TODO: This is really just not working at all!
                EarthDecorationEventHandler.allowOreGeneration = true;
                generateOres.invoke(biome.decorator, wrappedWorld, this.horizontalRandom);
                EarthDecorationEventHandler.allowOreGeneration = false;
            } catch (IllegalAccessException | InvocationTargetException e) {
                Terrarium.LOGGER.error("Failed to invoke ore generator", e);
            }
        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }
}
