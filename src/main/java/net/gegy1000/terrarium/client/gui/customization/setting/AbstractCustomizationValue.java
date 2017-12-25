package net.gegy1000.terrarium.client.gui.customization.setting;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.text.translation.I18n;

public abstract class AbstractCustomizationValue<T> implements CustomizationValue<T> {
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
        return I18n.translateToLocal(this.name);
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

    protected abstract void set(EarthGenerationSettings settings, T value);

    protected abstract T get(EarthGenerationSettings settings);
}
