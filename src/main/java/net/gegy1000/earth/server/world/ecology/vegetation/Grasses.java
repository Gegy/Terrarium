package net.gegy1000.earth.server.world.ecology.vegetation;

import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.world.gen.feature.WorldGenDoublePlant;
import net.minecraft.world.gen.feature.WorldGenTallGrass;

public final class Grasses {
    public static final float RADIUS = 4.0F;

    private static final WorldGenTallGrass GRASS_GENERATOR = new WorldGenTallGrass(BlockTallGrass.EnumType.GRASS);
    private static final WorldGenTallGrass FERN_GENERATOR = new WorldGenTallGrass(BlockTallGrass.EnumType.GRASS);

    private static final WorldGenDoublePlant TALL_GRASS_GENERATOR = new WorldGenDoublePlant();
    private static final WorldGenDoublePlant TALL_FERN_GENERATOR = new WorldGenDoublePlant();

    static {
        TALL_GRASS_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.GRASS);
        TALL_FERN_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.FERN);
    }

    public static final Vegetation GRASS = Vegetation.builder()
            .growthIndicator(GrowthIndicator.relaxed())
            .generator(GRASS_GENERATOR::generate)
            .build();

    public static final Vegetation TALL_GRASS = Vegetation.builder()
            .growthIndicator(GrowthIndicator.relaxed())
            .generator(TALL_GRASS_GENERATOR::generate)
            .build();

    public static final Vegetation FERN = Vegetation.builder()
            .growthIndicator(GrowthIndicator.relaxed())
            .generator(FERN_GENERATOR::generate)
            .build();

    public static final Vegetation TALL_FERN = Vegetation.builder()
            .growthIndicator(GrowthIndicator.relaxed())
            .generator(TALL_FERN_GENERATOR::generate)
            .build();
}
