package net.gegy1000.earth.server.world.pipeline.layer;

import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.ParentedDataLayer;
import net.gegy1000.terrarium.server.world.pipeline.adapter.debug.DebugImageWriter;
import net.gegy1000.terrarium.server.world.pipeline.layer.LayerContext;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;

public abstract class OsmWaterLayer extends ParentedDataLayer<ShortRasterTile, ShortRasterTile> {
    public static final int TYPE_MASK = 0x3;
    public static final short LAND = 0;
    public static final short OCEAN = 1;
    public static final short RIVER = 2;
    public static final short BANK = 3;

    protected static final short BANK_UP_FLAG = 0b100;
    protected static final short BANK_DOWN_FLAG = 0b1000;
    protected static final short FREE_FLOOD_FLAG = 0b10000;
    protected static final short CENTER_FLAG = 0b100000;

    public static final DebugImageWriter.ColorSelector<Short> BANK_DEBUG = value -> {
        if ((value & FREE_FLOOD_FLAG) != 0) {
            return 0x404040;
        } else if ((value & BANK_UP_FLAG) != 0) {
            return 0xFF0000;
        } else if ((value & BANK_DOWN_FLAG) != 0) {
            return 0xFFFF00;
        } else if ((value & CENTER_FLAG) != 0) {
            return 0x00FFFF;
        }
        int type = value & TYPE_MASK;
        switch (type) {
            case OCEAN:
                return 0x0000FF;
            case RIVER:
                return 0x00AAFF;
            case LAND:
                return 0x00FF00;
            case BANK:
                return 0xFFFFFF;
        }
        return 0;
    };

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
