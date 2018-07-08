package net.gegy1000.terrarium.server.world.rasterization;

import java.awt.Color;

public class ValueColor extends Color {
    private int value;

    public ValueColor() {
        super(0);
    }

    public void set(int value) {
        this.value = (value & 0xFF) << 16 | 0xFF000000;
    }

    @Override
    public int hashCode() {
        return this.value;
    }

    @Override
    public int getRGB() {
        return this.value;
    }
}
