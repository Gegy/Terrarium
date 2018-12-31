package net.gegy1000.terrarium.server.world.cover.generator.layer;

import net.minecraft.class_3630;
import net.minecraft.class_3663;

public class ConnectHorizontalLayer implements class_3663 {
    private final int connectValue;

    public ConnectHorizontalLayer(int connectValue) {
        this.connectValue = connectValue;
    }

    @Override
    public int sample(class_3630 context, int up, int right, int down, int left, int self) {
        boolean connectRight = right == this.connectValue;
        boolean connectLeft = left == this.connectValue;
        boolean connectDown = down == this.connectValue;
        if (connectRight && connectDown) {
            return this.connectValue;
        } else if (connectLeft && connectDown) {
            return this.connectValue;
        }
        return self;
    }
}
