package net.gegy1000.terrarium.client.gui.widget;

import com.google.common.collect.Lists;
import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class ToggleGuiWidget extends GuiButton implements TooltipRenderer {
    private final PropertyKey<Boolean> propertyKey;
    private final PropertyValue<Boolean> property;

    private boolean state;
    private float hoverTime;

    public ToggleGuiWidget(int buttonId, int x, int y, PropertyKey<Boolean> propertyKey, PropertyValue<Boolean> property) {
        super(buttonId, x, y, 150, 20, "");
        this.propertyKey = propertyKey;
        this.property = property;

        this.setState(property.get());
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            super.drawButton(mc, mouseX, mouseY, partialTicks);

            if (super.mousePressed(mc, mouseX, mouseY)) {
                this.hoverTime += partialTicks;
            } else {
                this.hoverTime = 0;
            }
        }
    }

    @Override
    public void renderTooltip(int mouseX, int mouseY) {
        if (this.hoverTime >= 15) {
            String name = TextFormatting.BLUE + this.propertyKey.getLocalizedName();
            String tooltip = TextFormatting.GRAY + this.propertyKey.getLocalizedTooltip();
            List<String> lines = Lists.newArrayList(name, tooltip);
            GuiRenderUtils.drawTooltip(lines, mouseX, mouseY);
        }
    }

    public void setState(boolean state) {
        this.state = state;
        this.property.set(state);

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
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.setState(!this.state);
            return true;
        }
        return false;
    }
}
