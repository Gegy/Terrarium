package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.ParentedDataLayer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.UnsignedByteRasterTile;
import net.minecraft.util.math.MathHelper;

public class SlopeProducerLayer extends ParentedDataLayer<UnsignedByteRasterTile, ShortRasterTile> {
    public SlopeProducerLayer(DataLayer<ShortRasterTile> parent) {
        super(parent);
    }

    @Override
    protected UnsignedByteRasterTile apply(LayerContext context, DataView view, ShortRasterTile parent, DataView parentView) {
        UnsignedByteRasterTile output = new UnsignedByteRasterTile(view);

        for (int localY = 0; localY < view.getHeight(); localY++) {
            for (int localX = 0; localX < view.getWidth(); localX++) {
                int parentX = localX + 1;
                int parentY = localY + 1;
                short current = parent.getShort(parentX, parentY);

                int topLeft = Math.abs(current - parent.getShort(parentX - 1, parentY - 1));
                int topRight = Math.abs(current - parent.getShort(parentX + 1, parentY - 1));
                int bottomLeft = Math.abs(current - parent.getShort(parentX - 1, parentY + 1));
                int bottomRight = Math.abs(current - parent.getShort(parentX + 1, parentY + 1));

                int maxSlope = (topLeft + topRight + bottomLeft + bottomRight) / 2;

                output.setByte(localX, localY, MathHelper.clamp(maxSlope, 0, 255));
            }
        }

        return output;
    }

    @Override
    public DataView getParentView(DataView view) {
        return new DataView(view.getX() - 1, view.getY() - 1, view.getWidth() + 2, view.getHeight() + 2);
    }
}
