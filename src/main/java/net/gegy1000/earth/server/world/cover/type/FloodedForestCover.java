package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.OutlineEdgeLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;

import java.awt.Color;

public abstract class FloodedForestCover extends ForestCover {
    public FloodedForestCover(Color approximateColor) {
        super(approximateColor);
    }

    @Override
    public abstract Surface createSurfaceGenerator(EarthCoverContext context);

    protected static class Surface extends ForestCover.Surface {
        protected final GenLayer waterSelector;

        protected Surface(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);

            // TODO: Reimplement
            GenLayer water = new SelectionSeedLayer(2, 2);
            water = new GenLayerFuzzyZoom(11000, water);
            water = new GenLayerVoronoiZoom(12000, water);
            water = new OutlineEdgeLayer(3, 13000, water);
            water = new GenLayerZoom(14000, water);

            this.waterSelector = water;
            this.waterSelector.initWorldGenSeed(context.getSeed());
        }
    }
}
