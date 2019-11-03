package net.gegy1000.terrarium.server.world.generator.customization.widget;

public enum InversePropertyConverter implements WidgetPropertyConverter {
    INSTANCE;

    @Override
    public double fromUser(double value) {
        return 1.0 / value;
    }

    @Override
    public double toUser(double value) {
        return 1.0 / value;
    }
}
