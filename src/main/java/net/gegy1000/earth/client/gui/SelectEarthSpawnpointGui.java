package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.client.gui.widget.map.PlaceSearchWidget;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapPoint;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapWidget;
import net.gegy1000.earth.client.gui.widget.map.component.MarkerMapComponent;
import net.gegy1000.earth.server.world.EarthWorldDefinition;
import net.gegy1000.earth.server.world.pipeline.source.GoogleGeocoder;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class SelectEarthSpawnpointGui extends GuiScreen {
    private static final int SELECT_BUTTON = 0;
    private static final int CANCEL_BUTTON = 1;
    private static final int SEARCH_FIELD = 2;

    private final EarthCustomizationGui parent;

    private SlippyMapWidget mapWidget;
    private MarkerMapComponent markerComponent;
    private PlaceSearchWidget searchWidget;

    public SelectEarthSpawnpointGui(EarthCustomizationGui parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        if (this.mapWidget != null) {
            this.mapWidget.close();
        }

        Keyboard.enableRepeatEvents(true);

        this.mapWidget = new SlippyMapWidget(20, 20, this.width - 40, this.height - 60);

        GenerationSettings settings = this.parent.getSettings();

        double latitude = settings.getDouble(EarthWorldDefinition.SPAWN_LATITUDE);
        double longitude = settings.getDouble(EarthWorldDefinition.SPAWN_LONGITUDE);
        this.markerComponent = new MarkerMapComponent(new SlippyMapPoint(latitude, longitude)).allowMovement();
        this.mapWidget.addComponent(this.markerComponent);

        this.searchWidget = new PlaceSearchWidget(SEARCH_FIELD, 25, 25, 200, 20, new GoogleGeocoder(), this::handleSearch);
        this.searchWidget.setFocused(true);

        this.addButton(new GuiButton(SELECT_BUTTON, this.width / 2 - 154, this.height - 28, 150, 20, I18n.format("gui.done")));
        this.addButton(new GuiButton(CANCEL_BUTTON, this.width / 2 + 4, this.height - 28, 150, 20, I18n.format("gui.cancel")));
    }

    private void handleSearch(double latitude, double longitude) {
        this.markerComponent.moveMarker(latitude, longitude);
        this.mapWidget.getMap().focus(latitude, longitude, 12);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            this.mc.displayGuiScreen(this.parent);
            if (button.id == SELECT_BUTTON) {
                SlippyMapPoint marker = this.markerComponent.getMarker();
                if (marker != null) {
                    this.parent.applySpawnpoint(marker.getLatitude(), marker.getLongitude());
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawDefaultBackground();
        this.mapWidget.draw(mouseX, mouseY, partialTicks);
        this.searchWidget.draw(mouseX, mouseY);
        this.drawCenteredString(this.fontRenderer, I18n.format("gui.earth.spawnpoint"), this.width / 2, 4, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.searchWidget.update();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.searchWidget.isFocused() && this.searchWidget.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.searchWidget.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        this.mapWidget.mouseClicked(mouseX, mouseY);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
        this.mapWidget.mouseDragged(mouseX, mouseY);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        this.mapWidget.mouseReleased(mouseX, mouseY);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.mapWidget.close();
        this.searchWidget.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }
}
