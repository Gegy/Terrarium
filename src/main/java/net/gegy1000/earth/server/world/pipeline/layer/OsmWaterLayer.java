package net.gegy1000.earth.server.world.pipeline.layer;

import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.ParentedDataLayer;
import net.gegy1000.terrarium.server.world.pipeline.layer.LayerContext;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;

public abstract class OsmWaterLayer extends ParentedDataLayer<ShortRasterTile, ShortRasterTile> {
    public static final int TYPE_MASK = 0x3;
    public static final short LAND = 0;
    public static final short OCEAN = 1;
    public static final short RIVER = 2;
    public static final short BANK = 3;

    private final DataLayer<OsmTile> osmLayer;

    protected OsmWaterLayer(DataLayer<ShortRasterTile> parent, DataLayer<OsmTile> osmLayer) {
        super(parent);
        this.osmLayer = osmLayer;
    }

    protected abstract ShortRasterTile applyWater(DataView view, ShortRasterTile waterTile, OsmTile osmTile);

    @Override
    protected ShortRasterTile apply(LayerContext context, DataView view, ShortRasterTile parent, DataView parentView) {
        OsmTile osmTile = context.apply(this.osmLayer, view);
        return this.applyWater(view, parent, osmTile);
    }

    @Override
    public DataView getParentView(DataView view) {
        return view;
    }
}
