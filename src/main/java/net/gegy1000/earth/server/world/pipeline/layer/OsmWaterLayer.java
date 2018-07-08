package net.gegy1000.earth.server.world.pipeline.layer;

import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.terrarium.server.world.pipeline.DataLayerProcessor;
import net.gegy1000.terrarium.server.world.pipeline.DataLayerProducer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;

public abstract class OsmWaterLayer implements DataLayerProcessor<ShortRasterTile, ShortRasterTile> {
    public static final int TYPE_MASK = 0x3;
    public static final short LAND = 0;
    public static final short OCEAN = 1;
    public static final short RIVER = 2;
    public static final short BANK = 3;

    private final DataLayerProducer<OsmTile> osmLayer;

    protected OsmWaterLayer(DataLayerProducer<OsmTile> osmLayer) {
        this.osmLayer = osmLayer;
    }

    @Override
    public void reset() {
        this.osmLayer.reset();
    }

    protected abstract ShortRasterTile applyWater(DataView view, ShortRasterTile waterTile, OsmTile osmTile);

    @Override
    public ShortRasterTile apply(DataView view, ShortRasterTile parent, DataView parentView) {
        OsmTile osmTile = this.osmLayer.apply(view);
        return this.applyWater(view, parent, osmTile);
    }

    @Override
    public DataView getParentView(DataView view) {
        return view;
    }
}
