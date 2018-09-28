package net.gegy1000.terrarium.client.gui.widget;

import com.google.common.collect.Lists;
import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.gegy1000.terrarium.server.world.generator.customization.property.CycleEnumProperty;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class CycleGuiWidget<T extends Enum & CycleEnumProperty> extends GuiButton implements TooltipRenderer {
    private final PropertyKey<T> propertyKey;
    private final PropertyValue<T> property;

    private final List<Runnable> listeners = new ArrayList<>();

    private int ordinal;
    private float hoverTime;

    public CycleGuiWidget(int buttonId, int x, int y, PropertyKey<T> propertyKey, PropertyValue<T> property) {
        super(buttonId, x, y, 150, 20, "");
        this.propertyKey = propertyKey;
        this.property = property;

        this.setOrdinal(property.get().ordinal());
    }

    public void addListener(Runnable listener) {
        this.listeners.add(listener);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            super.drawButton(mc, mouseX, mouseY, partialTicks);

            if (this.isSelected(mouseX, mouseY)) {
                this.hoverTime += partialTicks;
            } else {
                this.hoverTime = 0;
            }
        }
    }

    @Override
    public void renderTooltip(int mouseX, int mouseY) {
        if (this.hoverTime >= 15) {
            List<String> lines = this.getTooltip();
            GuiRenderUtils.drawTooltip(lines, mouseX, mouseY);
        }
    }

    private List<String> getTooltip() {
        String name = TextFormatting.BLUE + this.propertyKey.getLocalizedName();
        String tooltip = TextFormatting.GRAY + this.propertyKey.getLocalizedTooltip();

        List<String> lines = Lists.newArrayList(name, tooltip);

        for (T variant : this.getVariants()) {
            String descriptionKey = variant.getDescriptionKey();
            if (descriptionKey != null) {
                String translatedKey = I18n.format(variant.getTranslationKey());
                String translatedDescription = I18n.format(variant.getDescriptionKey());
                lines.add(variant.getFormatting() + String.format(" - %s: %s", translatedKey, TextFormatting.GRAY + translatedDescription));
            }
        }

        return lines;
    }

    public void setOrdinal(int ordinal) {
        if (ordinal != this.ordinal) {
            T[] constants = this.getVariants();
            this.property.set(constants[ordinal]);
            for (Runnable listener : this.listeners) {
                listener.run();
            }
        }

        this.ordinal = ordinal;

        T variant = this.getVariant(this.ordinal);
        String state = variant.getFormatting() + I18n.format(variant.getTranslationKey());
        this.displayString = String.format("%s: %s", this.propertyKey.getLocalizedName(), state);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (this.isSelected(mouseX, mouseY)) {
            T[] variants = this.getVariants();
            this.setOrdinal((this.ordinal + 1) % variants.length);
            return true;
        }
        return false;
    }

    private boolean isSelected(int mouseX, int mouseY) {
        return this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }

    private T getVariant(int ordinal) {
        T[] variants = this.getVariants();
        return variants[ordinal % variants.length];
    }

    private T[] getVariants() {
        return this.propertyKey.getType().getEnumConstants();
    }
}
