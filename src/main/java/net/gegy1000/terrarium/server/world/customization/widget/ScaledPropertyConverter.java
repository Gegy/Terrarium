package net.gegy1000.terrarium.server.world.customization.widget;

public class ScaledPropertyConverter implements WidgetPropertyConverter {
    private final double scale;

    public ScaledPropertyConverter(double scale) {
        this.scale = scale;
    }

    @Override
    public double fromUser(double value) {
        return value / this.scale;
    }

    @Override
    public double toUser(double value) {
        return value * this.scale;
    }
}
