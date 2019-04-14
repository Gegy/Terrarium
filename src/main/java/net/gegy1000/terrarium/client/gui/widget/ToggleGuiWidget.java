package net.gegy1000.terrarium.client.gui.widget;

import com.google.common.collect.Lists;
import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ToggleGuiWidget extends GuiButtonExt implements TooltipRenderer {
    private final PropertyKey<Boolean> propertyKey;
    private final PropertyValue<Boolean> property;

    private final List<Runnable> listeners = new ArrayList<>();

    private boolean state;
    private float hoverTime;
    private boolean locked;

    public ToggleGuiWidget(int buttonId, int x, int y, PropertyKey<Boolean> propertyKey, PropertyValue<Boolean> property) {
        super(buttonId, x, y, 150, 20, "");
        this.propertyKey = propertyKey;
        this.property = property;

        this.setState(property.get());
    }

    public void addListener(Runnable listener) {
        this.listeners.add(listener);
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        this.enabled = !locked;
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
        if (this.locked) {
            return Lists.newArrayList(TextFormatting.GRAY + I18n.format("property.terrarium.locked.name"));
        } else {
            String name = TextFormatting.BLUE + this.propertyKey.getLocalizedName();
            String tooltip = TextFormatting.GRAY + this.propertyKey.getLocalizedTooltip();
            return Lists.newArrayList(name, tooltip);
        }
    }

    public void setState(boolean state) {
        if (state != this.state) {
            this.property.set(state);
            for (Runnable listener : this.listeners) {
                listener.run();
            }
        }

        this.state = state;

        String stateKey = I18n.format(this.state ? "gui.yes" : "gui.no");
        if (this.state) {
            stateKey = TextFormatting.GREEN + stateKey;
        } else {
            stateKey = TextFormatting.RED + stateKey;
        }
        this.displayString = String.format("%s: %s", this.propertyKey.getLocalizedName(), stateKey);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (this.isSelected(mouseX, mouseY)) {
            if (!this.locked) {
                this.setState(!this.state);
            }
            return true;
        }
        return false;
    }

    private boolean isSelected(int mouseX, int mouseY) {
        return this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }
}
