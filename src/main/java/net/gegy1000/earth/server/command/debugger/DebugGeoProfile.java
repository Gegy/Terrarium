package net.gegy1000.earth.server.command.debugger;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.ecology.soil.SoilClass;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.function.Consumer;

public final class DebugGeoProfile {
    private static final TextComponentString EMPTY = new TextComponentString("");

    public final String name;
    public final double latitude;
    public final double longitude;

    public final float surfaceElevation;
    public final Cover cover;
    public final float meanTemperature;
    public final float minTemperature;
    public final int annualRainfall;

    public final SoilClass soilClass;
    public final int siltContent;
    public final int sandContent;
    public final int clayContent;
    public final int organicCarbonContent;
    public final int cationExchangeCapacity;
    public final float soilPh;

    DebugGeoProfile(
            String name, double latitude, double longitude,
            float surfaceElevation, Cover cover,
            float meanTemperature, float minTemperature, int annualRainfall,
            SoilClass soilClass,
            int siltContent, int sandContent, int clayContent,
            int organicCarbonContent, int cationExchangeCapacity,
            float soilPh
    ) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.surfaceElevation = surfaceElevation;
        this.cover = cover;
        this.meanTemperature = meanTemperature;
        this.minTemperature = minTemperature;
        this.annualRainfall = annualRainfall;
        this.soilClass = soilClass;
        this.siltContent = siltContent;
        this.sandContent = sandContent;
        this.clayContent = clayContent;
        this.organicCarbonContent = organicCarbonContent;
        this.cationExchangeCapacity = cationExchangeCapacity;
        this.soilPh = soilPh;
    }

    public void sendTo(EntityPlayerMP player) {
        String headerFormat = TextFormatting.UNDERLINE.toString() + TextFormatting.BOLD.toString();

        String header = String.format("Profile at %.5f, %.5f (%s)", this.latitude, this.longitude, this.name);
        player.sendMessage(new TextComponentString(headerFormat + header));
        player.sendMessage(EMPTY);

        this.sendCategory(player, "General", p -> {
            p.sendMessage(makeValue("Surface Elevation", "%.1fm", this.surfaceElevation));
            p.sendMessage(makeValue("Cover Type", "%s (%s)", this.cover, this.cover.id));
            p.sendMessage(makeValue("Mean Temperature", "%.1fC", this.meanTemperature));
            p.sendMessage(makeValue("Min Temperature", "%.1fC", this.minTemperature));
            p.sendMessage(makeValue("Annual Rainfall", "%smm", this.annualRainfall));
        });

        this.sendCategory(player, "Soil", p -> {
            p.sendMessage(makeValue("Soil Class", "%s (%s)", this.soilClass, this.soilClass.id));
            p.sendMessage(makeValue("Silt Content", "%s%%", this.siltContent));
            p.sendMessage(makeValue("Sand Content", "%s%%", this.sandContent));
            p.sendMessage(makeValue("Clay Content", "%s%%", this.clayContent));
            p.sendMessage(makeValue("Organic Carbon Content", "%s g/kg", this.organicCarbonContent));
            p.sendMessage(makeValue("Cation Exchange Capacity", "%s cmolc/kg", this.cationExchangeCapacity));
            p.sendMessage(makeValue("pH", "%s", this.soilPh));
        });
    }

    private void sendCategory(EntityPlayerMP player, String name, Consumer<EntityPlayerMP> send) {
        player.sendMessage(new TextComponentString(TextFormatting.UNDERLINE + name));
        player.sendMessage(EMPTY);
        send.accept(player);
        player.sendMessage(EMPTY);
    }

    private static ITextComponent makeValue(String key, String value, Object... args) {
        String keyFormat = TextFormatting.AQUA.toString();
        String valueFormat = TextFormatting.RESET.toString();
        return new TextComponentString(" " + keyFormat + key + ": " + valueFormat + String.format(value, args));
    }
}
