package net.gegy1000.terrarium.server.world.data.op;

import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.DataView;

public final class SampleOffsetOp {
    private final int x;
    private final int z;

    public SampleOffsetOp(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public <T>DataOp<T> apply(DataOp<T> op) {
        return DataOp.of((view, executor) -> {
            DataView offsetView = DataView.rect(
                    view.getX() + this.x, view.getY() + this.z,
                    view.getWidth(), view.getHeight()
            );
            return op.apply(offsetView, executor);
        });
    }
}
