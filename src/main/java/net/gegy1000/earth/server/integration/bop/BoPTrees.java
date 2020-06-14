package net.gegy1000.earth.server.integration.bop;

import biomesoplenty.api.block.IBlockPosQuery;
import biomesoplenty.api.enums.BOPTrees;
import biomesoplenty.api.enums.BOPWoods;
import biomesoplenty.api.generation.IGenerator;
import biomesoplenty.common.world.GeneratorRegistry;
import biomesoplenty.common.world.generator.tree.GeneratorBigTree;
import biomesoplenty.common.world.generator.tree.GeneratorBulbTree;
import biomesoplenty.common.world.generator.tree.GeneratorMahoganyTree;
import biomesoplenty.common.world.generator.tree.GeneratorPalmTree;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.earth.server.world.ecology.SoilPredicate;
import net.gegy1000.earth.server.world.ecology.maxent.MaxentGrowthIndicator;
import net.gegy1000.earth.server.world.ecology.vegetation.Vegetation;
import net.gegy1000.earth.server.world.ecology.vegetation.VegetationGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.WorldGenerator;

// TODO: Ensure CubicChunks support for all trees
public final class BoPTrees {
    public static final Vegetation MAHOGANY = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.MAHOGANY))
            .growthIndicator(Indicators.MAHOGANY)
            .build();

    public static final Vegetation PALM = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.PALM))
            .growthIndicator(Indicators.PALM)
            .build();

    public static final Vegetation EUCALYPTUS = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.EUCALYPTUS))
            .growthIndicator(Indicators.EUCALYPTUS)
            .build();

    public static final Vegetation MANGROVE = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.MANGROVE))
            .growthIndicator(Indicators.MANGROVE)
            .build();

    public static final Vegetation WILLOW = Vegetation.builder()
            .generator((world, random, pos) -> {
                if (random.nextInt(3) == 0) {
                    Generators.LARGE_WILLOW.generate(world, random, pos);
                } else {
                    Generators.WILLOW.generate(world, random, pos);
                }
            })
            .growthIndicator(Indicators.WILLOW)
            .build();

    public static final Vegetation EBONY = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.EBONY))
            .growthIndicator(Indicators.EBONY)
            .build();

    public static final Vegetation FIR = Vegetation.builder()
            .generator(VegetationGenerator.of(Generators.FIR))
            .growthIndicator(Indicators.FIR)
            .build();

    public static class Indicators {
        public static final GrowthIndicator MAHOGANY = maxentIndicator("mahogany").pow(1.8F);
        public static final GrowthIndicator PALM = maxentIndicator("palm").pow(3.0F);
        public static final GrowthIndicator EUCALYPTUS = maxentIndicator("eucalyptus").pow(3.0F);

        public static final GrowthIndicator MANGROVE = maxentIndicator("mangrove");
        public static final GrowthIndicator WILLOW = maxentIndicator("willow").pow(2.0F);
        public static final GrowthIndicator EBONY = maxentIndicator("ebony").pow(1.6F);
        public static final GrowthIndicator FIR = maxentIndicator("fir");

        private static GrowthIndicator maxentIndicator(String path) {
            return MaxentGrowthIndicator.tryParse(new ResourceLocation(TerrariumEarth.ID, "vegetation/models/trees/" + path + ".lambdas"))
                    .orElse(GrowthIndicator.no());
        }
    }

    static class Generators {
        static {
            registerPatchGenerator("patched_mangrove", PatchedMangroveTreeFeature.class, new PatchedMangroveTreeFeature.Builder());
            registerPatchGenerator("patched_bayou", PatchedBayouTreeFeature.class, new PatchedBayouTreeFeature.Builder());
            registerPatchGenerator("patched_taiga", PatchedTaigaTreeFeature.class, new PatchedTaigaTreeFeature.Builder());
        }

        private static final IBlockPosQuery ANY_SOIL = soilQuery(SoilPredicate.ANY);

        static final WorldGenerator MAHOGANY = new GeneratorMahoganyTree.Builder().placeOn(ANY_SOIL).create();
        static final WorldGenerator PALM = new GeneratorPalmTree.Builder().create();
        static final WorldGenerator EUCALYPTUS = new GeneratorBulbTree.Builder()
                .log(BOPWoods.EUCALYPTUS).leaves(BOPTrees.EUCALYPTUS)
                .minHeight(8).maxHeight(16)
                .placeOn(ANY_SOIL)
                .create();
        static final WorldGenerator MANGROVE = new PatchedMangroveTreeFeature.Builder()
                .log(BOPWoods.MANGROVE).leaves(BOPTrees.MANGROVE)
                .placeOn(ANY_SOIL)
                .create();
        static final WorldGenerator WILLOW = new PatchedBayouTreeFeature.Builder()
                .log(BOPWoods.WILLOW).leaves(BOPTrees.WILLOW)
                .minHeight(6).maxHeight(12).minLeavesRadius(1).leavesGradient(2)
                .placeOn(ANY_SOIL)
                .create();
        static final WorldGenerator LARGE_WILLOW = new PatchedBayouTreeFeature.Builder()
                .log(BOPWoods.WILLOW).leaves(BOPTrees.WILLOW)
                .minHeight(10).maxHeight(16).minLeavesRadius(2).leavesGradient(3)
                .placeOn(ANY_SOIL)
                .create();
        static final WorldGenerator EBONY = new GeneratorBigTree.Builder()
                .log(BOPWoods.EBONY).leaves(BOPTrees.EBONY)
                .minHeight(5).maxHeight(10).foliageHeight(1)
                .placeOn(ANY_SOIL)
                .create();
        static final WorldGenerator FIR = new PatchedTaigaTreeFeature.Builder()
                .log(BOPWoods.FIR).leaves(BOPTrees.FIR)
                .minHeight(8).maxHeight(16)
                .placeOn(ANY_SOIL)
                .create();

        @SuppressWarnings("unchecked")
        private static <T extends IGenerator> void registerPatchGenerator(String identifier, Class<T> generator, IGenerator.IGeneratorBuilder builder) {
            GeneratorRegistry.registerGenerator(identifier, generator, (IGenerator.IGeneratorBuilder<T>) builder);
        }

        private static IBlockPosQuery soilQuery(SoilPredicate predicate) {
            return (world, pos) -> predicate.canGrowOn(world, pos, world.getBlockState(pos));
        }
    }
}
