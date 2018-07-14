package net.gegy1000.earth.server.world.pipeline.layer;

import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.ParentedDataLayer;
import net.gegy1000.terrarium.server.world.pipeline.adapter.debug.DebugImageWriter;
import net.gegy1000.terrarium.server.world.pipeline.layer.LayerContext;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;

public class WaterProcessorLayer extends ParentedDataLayer<WaterRasterTile, ShortRasterTile> {
    protected static final DebugImageWriter.ColorSelector<Short> BANK_DEBUG = value -> {
        int type = value & WaterRasterTile.WATER_TYPE_MASK;
        switch (type) {
            case WaterRasterTile.OCEAN:
                return 0x0000FF;
            case WaterRasterTile.RIVER:
                return 0x00AAFF;
            case WaterRasterTile.LAND:
                return 0x00FF00;
        }
        return 0;
    };

    public WaterProcessorLayer(DataLayer<ShortRasterTile> parent) {
        super(parent);
    }

    @Override
    protected WaterRasterTile apply(LayerContext context, DataView view, ShortRasterTile parent, DataView parentView) {
        WaterRasterTile waterTile = new WaterRasterTile(view);
        for (int localZ = 0; localZ < view.getHeight(); localZ++) {
            for (int localX = 0; localX < view.getWidth(); localX++) {
                int waterType = parent.getShort(localX, localZ) & OsmWaterLayer.TYPE_MASK;
                switch (waterType) {
                    case OsmWaterLayer.LAND:
                    case OsmWaterLayer.BANK:
                        waterTile.setWaterType(localX, localZ, WaterRasterTile.LAND);
                        break;
                    case OsmWaterLayer.RIVER:
                        waterTile.setWaterType(localX, localZ, WaterRasterTile.RIVER);
                        break;
                    case OsmWaterLayer.OCEAN:
                        waterTile.setWaterType(localX, localZ, WaterRasterTile.OCEAN);
                        break;
                }
            }
        }
        return waterTile;
    }

    @Override
    public DataView getParentView(DataView view) {
        return view;
    }
}
