package net.gegy1000.terrarium.client.gui.customization.setting;

public interface CustomizationValue<T> {
    String getLocalizedName();

    String getLocalizedTooltip();

    void set(T value);

    T get();

    T getDefault();
}
