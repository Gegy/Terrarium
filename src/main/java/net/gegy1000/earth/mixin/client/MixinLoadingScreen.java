package net.gegy1000.earth.mixin.client;

import net.gegy1000.earth.client.LoadingWorldGetter;
import net.gegy1000.earth.server.world.EarthGeneratorType;
import net.gegy1000.terrarium.api.CustomLevelGenerator;
import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.menu.DownloadingTerrainGui;
import net.minecraft.client.gui.menu.WorkingGui;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TextFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ WorkingGui.class, DownloadingTerrainGui.class })
public class MixinLoadingScreen {
    @Inject(method = "draw(IIF)V", at = @At("RETURN"))
    private void draw(int mouseX, int mouseY, float delta, CallbackInfo callback) {
        renderAttribution();
    }

    private static void renderAttribution() {
        MinecraftClient client = MinecraftClient.getInstance();

        CustomLevelGenerator customGenerator = CustomLevelGenerator.unwrap(LoadingWorldGetter.getLoadingWorldType());
        if (customGenerator instanceof EarthGeneratorType) {
            int x = client.window.getScaledWidth() / 2;
            int y = client.window.getScaledHeight() - 38 - 11;

            String header = TextFormat.YELLOW.toString() + TextFormat.BOLD + I18n.translate("gui.earth.credits");
            GuiRenderUtils.drawStringCentered(header, x, y, 0xFFFFFF);
            GuiRenderUtils.drawStringCentered(TextFormat.GRAY + "NASA SRTM,", x, y + 11, 0xFFFFFF);
            GuiRenderUtils.drawStringCentered(TextFormat.GRAY + "ESA GlobCover,", x, y + 20, 0xFFFFFF);
            GuiRenderUtils.drawStringCentered(TextFormat.GRAY + "Google APIs,", x, y + 29, 0xFFFFFF);
            GuiRenderUtils.drawStringCentered(TextFormat.GRAY + "\u00a9 OpenStreetMap Contributors", x, y + 38, 0xFFFFFF);
        }
    }
}
