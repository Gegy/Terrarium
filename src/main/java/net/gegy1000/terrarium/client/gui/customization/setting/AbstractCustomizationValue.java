package net.gegy1000.terrarium.client.gui.customization.setting;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.text.translation.I18n;

public abstract class AbstractCustomizationValue<T> implements CustomizationValue<T> {
    private static final EarthGenerationSettings DEFAULT_SETTINGS = new EarthGenerationSettings();

    private final String name;

    private final EarthGenerationSettings settings;
    private final Runnable onChanged;

    protected AbstractCustomizationValue(String name, EarthGenerationSettings settings, Runnable onChanged) {
        this.name = name;
        this.settings = settings;
        this.onChanged = onChanged;
    }

    @Override
    public String getLocalizedName() {
        return I18n.translateToLocal(this.name + ".name");
    }

    @Override
    public String getLocalizedTooltip() {
        return I18n.translateToLocal(this.name + ".tooltip");
    }

    @Override
    public void set(T value) {
        if (!this.get(this.settings).equals(value)) {
            this.set(this.settings, value);
            this.onChanged.run();
        }
    }

    @Override
    public T get() {
        return this.get(this.settings);
    }

    @Override
    public T getDefault() {
        return this.get(DEFAULT_SETTINGS);
    }

    protected abstract void set(EarthGenerationSettings settings, T value);

    protected abstract T get(EarthGenerationSettings settings);
}
