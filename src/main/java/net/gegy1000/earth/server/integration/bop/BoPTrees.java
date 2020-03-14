package net.gegy1000.earth.server.integration.bop;

import biomesoplenty.api.enums.BOPTrees;
import biomesoplenty.api.enums.BOPWoods;
import biomesoplenty.common.world.generator.tree.GeneratorBulbTree;
import biomesoplenty.common.world.generator.tree.GeneratorMahoganyTree;
import biomesoplenty.common.world.generator.tree.GeneratorPalmTree;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.earth.server.world.ecology.maxent.MaxentGrowthIndicator;
import net.gegy1000.earth.server.world.ecology.vegetation.Vegetation;
import net.gegy1000.earth.server.world.ecology.vegetation.VegetationGenerator;
import net.minecraft.util.ResourceLocation;

// TODO: Ensure CubicChunks support for all trees
public final class BoPTrees {
    public static final Vegetation MAHOGANY = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.MAHOGANY))
            .growthIndicator(maxentIndicator("mahogany"))
            .build();

    public static final Vegetation PALM = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.PALM))
            .growthIndicator(maxentIndicator("palm").pow(3.0))
            .build();

    public static final Vegetation EUCALYPTUS = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.EUCALYPTUS))
            .growthIndicator(maxentIndicator("eucalyptus").pow(3.0))
            .build();

    private static GrowthIndicator maxentIndicator(String path) {
        return MaxentGrowthIndicator.tryParse(new ResourceLocation(TerrariumEarth.ID, "vegetation/models/trees/" + path + ".lambdas"))
                .orElse(GrowthIndicator.no());
    }

    static class Generators {
        static final GeneratorMahoganyTree MAHOGANY = new GeneratorMahoganyTree.Builder().create();
        static final GeneratorPalmTree PALM = new GeneratorPalmTree.Builder().create();
        static final GeneratorBulbTree EUCALYPTUS = new GeneratorBulbTree.Builder()
                .log(BOPWoods.EUCALYPTUS).leaves(BOPTrees.EUCALYPTUS)
                .minHeight(8).maxHeight(16)
                .create();
    }
}
