package net.gegy1000.terrarium.client.toast;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

public class DataFailToast implements IToast {
    private static final long VISIBLE_TIME = 8000;

    private final int failCount;

    public DataFailToast(int failCount) {
        this.failCount = failCount;
    }

    @Override
    public Visibility draw(GuiToast gui, long delta) {
        Minecraft mc = gui.getMinecraft();

        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        mc.getTextureManager().bindTexture(TEXTURE_TOASTS);
        gui.drawTexturedModalRect(0, 0, 0, 0, 160, 32);

        mc.fontRenderer.drawString(I18n.format("toast.terrarium.data_failure.title"), 8, 7, 0xFFFFFF00);
        mc.fontRenderer.drawString(I18n.format("toast.terrarium.data_failure.desc", this.failCount), 8, 18, 0xFFFFFFFF);

        return delta >= VISIBLE_TIME ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
    }
}
