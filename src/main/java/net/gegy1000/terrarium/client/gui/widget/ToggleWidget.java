package net.gegy1000.terrarium.client.gui.widget;

import com.google.common.collect.Lists;
import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.gegy1000.terrarium.client.gui.customization.setting.CustomizationValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class ToggleWidget extends GuiButton implements TooltipRenderer {
    private final CustomizationValue<Boolean> setting;

    private boolean state;
    private float hoverTime;

    public ToggleWidget(int buttonId, int x, int y, CustomizationValue<Boolean> setting) {
        super(buttonId, x, y, 150, 20, "");
        this.setting = setting;

        this.setState(setting.get());
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
    public void renderTooltip(Minecraft mc, int mouseX, int mouseY, int width, int height) {
        if (this.hoverTime >= 15) {
            String name = TextFormatting.BLUE + this.setting.getLocalizedName();
            String tooltip = TextFormatting.GRAY + this.setting.getLocalizedTooltip();
            String defaults = TextFormatting.YELLOW + I18n.translateToLocalFormatted("setting.terrarium.default.name", this.setting.getDefault());
            List<String> lines = Lists.newArrayList(name, tooltip, defaults);
            GuiRenderUtils.drawTooltip(lines, mouseX, mouseY);
        }
    }

    public void setState(boolean state) {
        this.state = state;
        this.setting.set(state);

        String stateKey = I18n.translateToLocal(this.state ? "gui.yes" : "gui.no");
        if (this.state) {
            stateKey = TextFormatting.GREEN + stateKey;
        } else {
            stateKey = TextFormatting.RED + stateKey;
        }
        this.displayString = String.format("%s: %s", this.setting.getLocalizedName(), stateKey);
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
