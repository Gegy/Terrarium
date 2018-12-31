package net.gegy1000.terrarium.server.world.customization.property;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;

import javax.annotation.Nullable;

public abstract class PropertyKey<T> {
    private final String identifier;
    private final Class<T> type;

    public PropertyKey(String identifier, Class<T> type) {
        this.identifier = identifier;
        this.type = type;
    }

    public abstract <D> D serialize(DynamicOps<D> ops, PropertyValue<T> value);

    @Nullable
    public abstract <D> PropertyValue<T> parseValue(Dynamic<D> element);

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

    @Environment(EnvType.CLIENT)
    public String getLocalizedName() {
        return I18n.translate("property.terrarium." + this.identifier + ".name");
    }

    @Environment(EnvType.CLIENT)
    public String getLocalizedTooltip() {
        return I18n.translate("property.terrarium." + this.identifier + ".tooltip");
    }
}
