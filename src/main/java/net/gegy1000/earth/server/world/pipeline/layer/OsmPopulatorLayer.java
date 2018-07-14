package net.gegy1000.earth.server.world.pipeline.layer;

import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.ParentedDataLayer;
import net.gegy1000.terrarium.server.world.pipeline.layer.LayerContext;

public class OsmPopulatorLayer extends ParentedDataLayer<OsmTile, OsmTile> {
    public OsmPopulatorLayer(DataLayer<OsmTile> parent) {
        super(parent);
    }

    @Override
    protected OsmTile apply(LayerContext context, DataView view, OsmTile parent, DataView parentView) {
        return parent;
    }

    @Override
    public DataView getParentView(DataView view) {
        return view.grow(8, 8, 8, 8);
    }
}
