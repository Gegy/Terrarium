package net.gegy1000.terrarium.server.world.generator.customization.property;

import com.google.gson.JsonElement;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public abstract class PropertyKey<T> {
    private final String identifier;
    private final Class<T> type;

    public PropertyKey(String identifier, Class<T> type) {
        this.identifier = identifier;
        this.type = type;
    }

    public abstract JsonElement serializeValue(PropertyValue<T> value);

    public abstract PropertyValue<T> makeValue(T value);

    @Nullable
    public abstract PropertyValue<T> parseValue(JsonElement element);

    public String getIdentifier() {
        return this.identifier;
    }

    public Class<T> getType() {
        return this.type;
    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PropertyKey && obj.getClass() == this.getClass() && ((PropertyKey) obj).getIdentifier().equals(this.identifier);
    }

    @SideOnly(Side.CLIENT)
    public String getLocalizedName() {
        return I18n.format("property.terrarium." + this.identifier + ".name");
    }

    @SideOnly(Side.CLIENT)
    public String getLocalizedTooltip() {
        return I18n.format("property.terrarium." + this.identifier + ".tooltip");
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + this.identifier + ")";
    }
}
