package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataLayerProcessor;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.minecraft.util.math.MathHelper;

public class SlopeProducerLayer implements DataLayerProcessor<ByteRasterTile, ShortRasterTile> {
    @Override
    public ByteRasterTile apply(DataView view, ShortRasterTile parent, DataView parentView) {
        ByteRasterTile output = new ByteRasterTile(view);

        for (int localY = 0; localY < view.getHeight(); localY++) {
            for (int localX = 0; localX < view.getWidth(); localX++) {
                int parentX = localX + 1;
                int parentY = localY + 1;
                short current = parent.getShort(parentX, parentY);

                int left = Math.abs(current - parent.getShort(parentX - 1, parentY));
                int right = Math.abs(current - parent.getShort(parentX + 1, parentY));
                int top = Math.abs(current - parent.getShort(parentX, parentY - 1));
                int bottom = Math.abs(current - parent.getShort(parentX, parentY + 1));

                int maxSlope = Math.max(left, Math.max(right, Math.max(top, bottom)));
                output.setByte(localX, localY, (byte) MathHelper.clamp(maxSlope, 0, 255));
            }
        }

        return output;
    }

    @Override
    public DataView getParentView(DataView view) {
        return new DataView(view.getX() - 1, view.getY() - 1, view.getWidth() + 2, view.getHeight() + 2);
    }
}
