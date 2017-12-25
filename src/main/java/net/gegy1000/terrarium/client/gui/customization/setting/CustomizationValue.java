package net.gegy1000.terrarium.client.gui.customization.setting;

public interface CustomizationValue<T> {
    String getLocalizedName();

    void set(T value);

    T get();
}
