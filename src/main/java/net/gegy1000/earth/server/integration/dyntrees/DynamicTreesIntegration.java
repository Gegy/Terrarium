package net.gegy1000.earth.server.integration.dyntrees;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.gegy1000.earth.server.event.CollectTreeGeneratorsEvent;
import net.gegy1000.earth.server.world.ecology.SoilPredicate;
import net.gegy1000.earth.server.world.ecology.vegetation.TreeGenerators;
import net.gegy1000.earth.server.world.ecology.vegetation.VegetationGenerator;
import net.gegy1000.terrarium.server.util.Lazy;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Optional;

public final class DynamicTreesIntegration {
    private static final Lazy<Optional<Species>> ACACIA = species("acacia");
    private static final Lazy<Optional<Species>> BIRCH = species("birch");
    private static final Lazy<Optional<Species>> JUNGLE = species("jungle");
    private static final Lazy<Optional<Species>> OAK = species("oak");
    private static final Lazy<Optional<Species>> SPRUCE = species("spruce");

    private static Lazy<Optional<Species>> species(String name) {
        return Lazy.ofRegistry(Species.REGISTRY, new ResourceLocation("dynamictrees", name));
    }

    public static void setup() {
        MinecraftForge.EVENT_BUS.register(DynamicTreesIntegration.class);
    }

    @SubscribeEvent
    public static void onCollectTreeGenerators(CollectTreeGeneratorsEvent event) {
        TreeGenerators generators = event.getGenerators();

        ACACIA.get().ifPresent(acacia -> generators.acacia = generator(acacia, 3, 5));
        BIRCH.get().ifPresent(birch -> generators.birch = generator(birch, 2, 4));
        JUNGLE.get().ifPresent(jungle -> generators.jungle = generator(jungle, 3, 5));
        OAK.get().ifPresent(oak -> generators.oak = generator(oak, 2, 4));
        SPRUCE.get().ifPresent(spruce -> {
            VegetationGenerator generator = generator(spruce, 3, 5);
            generators.spruce = generator;
            generators.pine = generator;
        });
    }

    private static VegetationGenerator generator(Species species, int minRadius, int maxRadius) {
        return (world, random, pos) -> {
            BlockPos soilPos = pos.down();
            if (!SoilPredicate.ANY.canGrowOn(world, soilPos)) {
                return;
            }

            Biome biome = world.getBiome(pos);
            int radius = random.nextInt(maxRadius - minRadius + 1) + minRadius;
            ChunkPos columnPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
            SafeChunkBounds safeBounds = new SafeChunkBounds(world, columnPos);
            species.generate(world, soilPos, biome, random, radius, safeBounds);
        };
    }
}
