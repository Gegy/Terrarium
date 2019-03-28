package net.gegy1000.earth.server.world.pipeline.layer;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.pipeline.source.WorldClimateDataset;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataTileKey;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.layer.LayerContext;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.FloatRasterTile;

import java.util.Collection;
import java.util.Collections;

public class TemperatureSampleLayer implements DataLayer<FloatRasterTile> {
    @Override
    public FloatRasterTile apply(LayerContext context, DataView view) {
        WorldClimateDataset climateDataset = TerrariumEarth.getClimateDataset();

        int width = view.getWidth();
        int height = view.getHeight();

        float[] buffer = new float[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer[x + y * width] = climateDataset.getAverageTemperature(view.getX() + x, view.getY() + y);
            }
        }

        return new FloatRasterTile(buffer, width, height);
    }

    @Override
    public Collection<DataTileKey<?>> getRequiredData(LayerContext context, DataView view) {
        return Collections.emptySet();
    }
}
