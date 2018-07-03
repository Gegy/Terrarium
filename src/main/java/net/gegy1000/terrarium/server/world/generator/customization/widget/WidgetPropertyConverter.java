package net.gegy1000.terrarium.server.world.generator.customization.widget;

public interface WidgetPropertyConverter {
    double fromUser(double value);

    double toUser(double value);
}
