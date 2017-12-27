package net.gegy1000.terrarium.client.gui.customization.setting;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;

// TODO: Better names! Also better spawn point (accurate + good choice) and further tweaking of them
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Terrarium.MODID, value = Side.CLIENT)
public class SettingPreset implements IForgeRegistryEntry<SettingPreset> {
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Terrarium.MODID, "setting_presets");

    private String name;
    private String icon;
    private EarthGenerationSettings settings;

    private ResourceLocation key;

    @SubscribeEvent
    public static void onConstructRegistries(RegistryEvent.NewRegistry event) {
        new RegistryBuilder<SettingPreset>()
                .setType(SettingPreset.class)
                .setName(REGISTRY_NAME)
                .setDefaultKey(new ResourceLocation(Terrarium.MODID, "default"))
                .disableSaving()
                .create();
    }

    @SubscribeEvent
    public static void onRegister(RegistryEvent.Register<SettingPreset> event) {
        Gson gson = new Gson();

        for (ModContainer mod : Loader.instance().getActiveModList()) {
            CraftingHelper.findFiles(mod, "assets/" + mod.getModId() + "/earth_presets", root -> true, (root, file) -> {
                String relative = root.relativize(file).toString();
                if (!"json".equals(FilenameUtils.getExtension(file.toString()))) {
                    return true;
                }

                String name = FilenameUtils.removeExtension(relative).replaceAll("\\\\", "/");
                ResourceLocation key = new ResourceLocation(mod.getModId(), name);

                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    SettingPreset preset = gson.fromJson(reader, SettingPreset.class);
                    preset.setRegistryName(key);
                    event.getRegistry().register(preset);
                    return true;
                } catch (JsonParseException e) {
                    Terrarium.LOGGER.error("Parsing error loading preset {}", key, e);
                } catch (IOException e) {
                    Terrarium.LOGGER.error("Couldn't read preset {} from {}", key, file, e);
                }

                return false;
            });
        }
    }

    public static IForgeRegistry<SettingPreset> getRegistry() {
        return GameRegistry.findRegistry(SettingPreset.class);
    }

    public String getLocalizedName() {
        return I18n.translateToLocal("preset." + this.name + ".name");
    }

    public String getLocalizedDescription() {
        return I18n.translateToLocal("preset." + this.name + ".desc");
    }

    public ResourceLocation getIcon() {
        ResourceLocation icon = new ResourceLocation(this.icon);
        return new ResourceLocation(icon.getResourceDomain(), "textures/preset/" + icon.getResourcePath() + ".png");
    }

    public EarthGenerationSettings getSettings() {
        return this.settings;
    }

    public void apply(EarthGenerationSettings settings) {
        settings.spawnLatitude = this.settings.spawnLatitude;
        settings.spawnLongitude = this.settings.spawnLongitude;
        settings.buildings = this.settings.buildings;
        settings.streets = this.settings.streets;
        settings.decorate = this.settings.decorate;
        settings.worldScale = this.settings.worldScale;
        settings.terrainHeightScale = this.settings.terrainHeightScale;
        settings.heightOffset = this.settings.heightOffset;
        settings.scatterRange = this.settings.scatterRange;
        settings.mapFeatures = this.settings.mapFeatures;
        settings.caveGeneration = this.settings.caveGeneration;
        settings.resourceGeneration = this.settings.resourceGeneration;
    }

    @Override
    public SettingPreset setRegistryName(ResourceLocation name) {
        this.key = name;
        return this;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return this.key;
    }

    @Override
    public Class<SettingPreset> getRegistryType() {
        return SettingPreset.class;
    }
}
