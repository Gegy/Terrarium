package net.gegy1000.earth.mixin.client;

import net.gegy1000.earth.api.WidgetArea;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TextFieldWidget.class)
public class MixinTextField implements WidgetArea {
    @Shadow
    private int x;
    @Shadow
    private int y;
    @Final
    @Shadow
    private int width;
    @Final
    @Shadow
    private int height;

    @Override
    public int getWidgetX() {
        return this.x;
    }

    @Override
    public int getWidgetY() {
        return this.y;
    }

    @Override
    public int getWidgetWidth() {
        return this.width;
    }

    @Override
    public int getWidgetHeight() {
        return this.height;
    }
}
