package net.gegy1000.earth.client.gui.widget.map.component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.earth.client.gui.widget.map.SlippyMap;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapPoint;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class MarkerMapComponent implements MapComponent {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final Identifier WIDGETS_TEXTURE = new Identifier(Terrarium.MODID, "textures/gui/widgets.png");

    private SlippyMapPoint marker;
    private boolean canMove;

    public MarkerMapComponent(SlippyMapPoint marker) {
        this.marker = marker;
    }

    public MarkerMapComponent() {
        this(null);
    }

    public MarkerMapComponent allowMovement() {
        this.canMove = true;
        return this;
    }

    @Override
    public void onDrawMap(SlippyMap map, double mouseX, double mouseY) {
        if (this.marker != null) {
            double scale = CLIENT.window.method_4495();

            double markerX = this.marker.getX(map.getCameraZoom()) - map.getCameraX();
            double markerY = this.marker.getY(map.getCameraZoom()) - map.getCameraY();

            CLIENT.getTextureManager().bindTexture(WIDGETS_TEXTURE);

            int x = MathHelper.floor(markerX - 4 * scale);
            int y = MathHelper.floor(markerY - 8 * scale);
            Gui.drawTexturedRect(x, y, 0.0F, 32.0F, 16, 16, MathHelper.floor(8 * scale), MathHelper.floor(8 * scale), 256, 256);
        }
    }

    @Override
    public void onMapClicked(SlippyMap map, double mouseX, double mouseY) {
        if (this.canMove) {
            double scale = CLIENT.window.method_4495();
            this.marker = new SlippyMapPoint(mouseX * scale + map.getCameraX(), mouseY * scale + map.getCameraY(), map.getCameraZoom());
        }
    }

    public void moveMarker(double latitude, double longitude) {
        this.marker = new SlippyMapPoint(latitude, longitude);
    }

    public SlippyMapPoint getMarker() {
        return this.marker;
    }
}
