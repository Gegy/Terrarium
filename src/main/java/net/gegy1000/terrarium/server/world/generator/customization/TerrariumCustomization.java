package net.gegy1000.terrarium.server.world.generator.customization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.gegy1000.terrarium.server.world.generator.customization.widget.CustomizationCategory;
import net.gegy1000.terrarium.server.world.generator.customization.widget.CustomizationWidget;

import java.util.Collection;

public class TerrariumCustomization {
    private final Collection<CustomizationCategory> categories;

    public TerrariumCustomization(Collection<CustomizationCategory> categories) {
        this.categories = categories;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Collection<CustomizationCategory> getCategories() {
        return this.categories;
    }

    public static class Builder {
        private final ImmutableList.Builder<CustomizationCategory> categories = new ImmutableList.Builder<>();

        private Builder() {
        }

        public Builder withCategory(String identifier, CustomizationWidget... widgets) {
            this.categories.add(new CustomizationCategory(identifier, Lists.newArrayList(widgets)));
            return this;
        }

        public TerrariumCustomization build() {
            return new TerrariumCustomization(this.categories.build());
        }
    }
}
