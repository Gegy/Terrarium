package net.gegy1000.terrarium.server.world.customization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class TerrariumPreset {
    private final String name;
    private final Identifier worldType;
    private final Identifier icon;
    private final Dynamic<JsonElement> settings;

    public TerrariumPreset(String name, Identifier worldType, Identifier icon, Dynamic<JsonElement> settings) {
        this.name = name;
        this.worldType = worldType;
        this.icon = new Identifier(icon.getNamespace(), "textures/preset/" + icon.getNamespace() + ".png");
        this.settings = settings;
    }

    public static TerrariumPreset parse(JsonObject root) {
        String name = JsonHelper.getString(root, "name");
        Identifier worldType = new Identifier(JsonHelper.getString(root, "world_type"));
        Identifier icon = new Identifier(JsonHelper.getString(root, "icon"));

        JsonObject properties = JsonHelper.getObject(root, "properties");
        return new TerrariumPreset(name, worldType, icon, new Dynamic<>(JsonOps.INSTANCE, properties));
    }

    @Environment(EnvType.CLIENT)
    public String getLocalizedName() {
        return I18n.translate("preset." + this.name + ".name");
    }

    @Environment(EnvType.CLIENT)
    public String getLocalizedDescription() {
        return I18n.translate("preset." + this.name + ".desc");
    }

    public Identifier getIcon() {
        return this.icon;
    }

    public GenerationSettings createSettings(PropertyPrototype prototype) {
        return GenerationSettings.deserialize(prototype, this.settings);
    }

    public Identifier getWorldType() {
        return this.worldType;
    }
}
