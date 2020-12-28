package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.client.gui.widget.map.PlaceSearchWidget;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapPoint;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapWidget;
import net.gegy1000.earth.client.gui.widget.map.component.MarkerMapComponent;
import net.gegy1000.terrarium.client.gui.widget.CopyBoxWidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class EarthLocateGui extends GuiScreen {
    private static final int TELEPORT_BUTTON = 0;
    private static final int CANCEL_BUTTON = 1;
    private static final int SEARCH_FIELD = 2;

    private final double latitude;
    private final double longitude;

    private SlippyMapWidget mapWidget;

    private MarkerMapComponent markerComponent;
    private PlaceSearchWidget searchWidget;
    private CopyBoxWidget locationBox;

    public EarthLocateGui(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public void initGui() {
        if (this.mapWidget != null) {
            this.mapWidget.close();
        }

        this.buttonList.clear();
        this.addButton(new GuiButton(TELEPORT_BUTTON, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.button.earth.teleport")));
        this.addButton(new GuiButton(CANCEL_BUTTON, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));

        this.mapWidget = new SlippyMapWidget(60, 20, this.width - 120, this.height - 100);
        this.mapWidget.getMap().focus(this.latitude, this.longitude, 11);

        this.markerComponent = new MarkerMapComponent(new SlippyMapPoint(this.latitude, this.longitude )).allowMovement();
        this.markerComponent.setOffsetX(16.0F);
        this.markerComponent.setOffsetY(32.0F);

        this.mapWidget.addComponent(this.markerComponent);
        this.mapWidget.addComponent(new MarkerMapComponent(new SlippyMapPoint(this.latitude, this.longitude)));

        int copyBoxWidth = 240;
        String locationText = String.format("%.5f, %.5f", this.latitude, this.longitude);
        this.locationBox = new CopyBoxWidget((this.width - copyBoxWidth) / 2, this.height - 75, copyBoxWidth, 20, locationText, this.fontRenderer);

        this.searchWidget = new PlaceSearchWidget(SEARCH_FIELD, 65, 25, 180, 20, TerrariumEarth.getPreferredGeocoder(), this::handleSearch);
    }

    private void handleSearch(double latitude, double longitude) {
        this.markerComponent.moveMarker(latitude, longitude);
        this.mapWidget.getMap().focus(latitude, longitude, 12);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.visible) {
            switch (button.id)
            {
                case TELEPORT_BUTTON:
                    SlippyMapPoint marker = this.markerComponent.getMarker();
                    this.sendChatMessage(String.format("/geotp %s %s", marker.getLatitude(), marker.getLongitude()));
                    this.mc.displayGuiScreen(null);
                    break;
                case CANCEL_BUTTON:
                    this.mc.displayGuiScreen(null);
                    break;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        this.mapWidget.draw(mouseX, mouseY, partialTicks);
        this.searchWidget.draw(mouseX, mouseY);

        this.drawCenteredString(this.fontRenderer, I18n.format("gui.earth.locate"), this.width / 2, 4, 0xFFFFFF);

        this.locationBox.draw(mouseX, mouseY);

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
        this.mapWidget.mouseClicked(mouseX, mouseY, mouseButton);
        this.locationBox.mouseClicked(mouseX, mouseY);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
        this.mapWidget.mouseDragged(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        this.mapWidget.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.mapWidget.close();
        this.searchWidget.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }
}
