package net.gegy1000.terrarium.server.world.cover.generator;

import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.ReplaceRandomLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

public abstract class ClosedForestCover extends ForestCover {
    public ClosedForestCover(CoverType type) {
        super(type);
    }

    @Override
    public GenLayer createCoverSelector() {
        GenLayer cover = new SelectionSeedLayer(2, 1);
        cover = new ReplaceRandomLayer(ForestCover.LAYER_PRIMARY, ForestCover.LAYER_DIRT, 2, 6000, cover);
        cover = new GenLayerVoronoiZoom(7000, cover);
        cover = new ReplaceRandomLayer(ForestCover.LAYER_DIRT, ForestCover.LAYER_PODZOL, 2, 8000, cover);
        cover = new GenLayerFuzzyZoom(9000, cover);
        return cover;
    }

    @Override
    public int getMaxHeightOffset() {
        return 6;
    }
}
