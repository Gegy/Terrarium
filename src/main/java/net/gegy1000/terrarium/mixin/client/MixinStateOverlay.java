package net.gegy1000.terrarium.mixin.client;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.ClientEventHandler;
import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.gegy1000.terrarium.server.world.pipeline.source.LoadingState;
import net.gegy1000.terrarium.server.world.pipeline.source.LoadingStateHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TextFormat;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinStateOverlay {
    private static final Identifier WIDGETS_TEXTURE = new Identifier(Terrarium.MODID, "textures/gui/widgets.png");

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "method_3192", at = @At("TAIL"))
    private void onRender(float delta, long time, boolean flag, CallbackInfo callback) {
        if (!this.client.field_1743) {
            Mouse mouse = this.client.mouse;
            render(mouse.getX(), mouse.getY());
        }
    }

    private static void render(double mouseX, double mouseY) {
        LoadingState state = LoadingStateHandler.getDisplayState();

        if (state != null) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(WIDGETS_TEXTURE);

            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            int animationTicks = ClientEventHandler.getGameTicks() % 40 / 5;
            int frame = animationTicks >= 5 ? 3 - (animationTicks - 5) : animationTicks;

            Gui.drawTexturedRect(10, 10, frame * 10, state.getTextureY(), 10, 10, 256, 256);

            if (mouseX >= 10 && mouseY >= 10 && mouseX <= 20 && mouseY <= 20) {
                String name = TextFormat.WHITE + I18n.translate(state.getLanguageKey() + ".name");
                String tooltip = TextFormat.GRAY + I18n.translate(state.getLanguageKey() + ".tooltip");
                GuiRenderUtils.drawTooltip(Lists.newArrayList(name, tooltip), mouseX, mouseY);
            }
        }
    }
}
