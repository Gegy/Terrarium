package net.gegy1000.terrarium.client.toast;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;

public class DataFailToast implements Toast {
    private static final long VISIBLE_TIME = 8000;

    private final int failCount;

    public DataFailToast(int failCount) {
        this.failCount = failCount;
    }

    @Override
    public class_369 draw(ToastManager manager, long delta) {
        MinecraftClient client = MinecraftClient.getInstance();

        GlStateManager.disableLighting();
        GlStateManager.enableAlphaTest();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        client.getTextureManager().bindTexture(TOASTS_TEX);
        manager.drawTexturedRect(0, 0, 0, 0, 160, 32);

        client.fontRenderer.draw(I18n.translate("toast.terrarium.data_failure.title"), 8, 7, 0xFFFFFF00);
        client.fontRenderer.draw(I18n.translate("toast.terrarium.data_failure.desc", this.failCount), 8, 18, 0xFFFFFFFF);

        return delta >= VISIBLE_TIME ? class_369.HIDE : class_369.SHOW;
    }
}
