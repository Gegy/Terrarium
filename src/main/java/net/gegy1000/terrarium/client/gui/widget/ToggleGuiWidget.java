package net.gegy1000.terrarium.client.gui.widget;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.gegy1000.terrarium.server.world.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.customization.property.PropertyValue;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TextFormat;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ToggleGuiWidget extends ButtonWidget implements TooltipRenderer {
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
    public void draw(int mouseX, int mouseY, float delta) {
        if (this.visible) {
            super.draw(mouseX, mouseY, delta);

            if (this.isSelected(mouseX, mouseY)) {
                this.hoverTime += delta;
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
            return Lists.newArrayList(TextFormat.GRAY + I18n.translate("property.terrarium.locked.name"));
        } else {
            String name = TextFormat.BLUE + this.propertyKey.getLocalizedName();
            String tooltip = TextFormat.GRAY + this.propertyKey.getLocalizedTooltip();
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

        String stateKey = I18n.translate(this.state ? "gui.yes" : "gui.no");
        if (this.state) {
            stateKey = TextFormat.GREEN + stateKey;
        } else {
            stateKey = TextFormat.RED + stateKey;
        }
        this.text = String.format("%s: %s", this.propertyKey.getLocalizedName(), stateKey);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isSelected(mouseX, mouseY)) {
            if (!this.locked) {
                this.setState(!this.state);
            }
            return true;
        }
        return false;
    }
}
