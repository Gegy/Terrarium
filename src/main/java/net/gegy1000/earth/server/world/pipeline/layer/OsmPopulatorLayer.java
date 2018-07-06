package net.gegy1000.earth.server.world.pipeline.layer;

import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.terrarium.server.world.pipeline.DataLayerProcessor;
import net.gegy1000.terrarium.server.world.pipeline.DataView;

public class OsmPopulatorLayer implements DataLayerProcessor<OsmTile, OsmTile> {
    @Override
    public OsmTile apply(DataView view, OsmTile parent, DataView parentView) {
        return parent;
    }

    @Override
    public DataView getParentView(DataView view) {
        return view.grow(8, 8, 8, 8);
    }
}
