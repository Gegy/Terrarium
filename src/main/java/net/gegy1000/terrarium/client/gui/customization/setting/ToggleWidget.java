package net.gegy1000.terrarium.client.gui.customization.setting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ToggleWidget extends GuiButton {
    private final CustomizationValue<Boolean> setting;

    private boolean state;

    public ToggleWidget(int buttonId, int x, int y, CustomizationValue<Boolean> setting) {
        super(buttonId, x, y, 150, 20, "");
        this.setting = setting;

        this.setState(setting.get());
    }

    public void setState(boolean state) {
        this.state = state;
        this.setting.set(state);

        String stateKey = I18n.translateToLocal(this.state ? "gui.yes" : "gui.no");
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
