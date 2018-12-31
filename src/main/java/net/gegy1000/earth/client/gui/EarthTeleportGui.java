package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.client.gui.widget.map.PlaceSearchWidget;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapPoint;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapWidget;
import net.gegy1000.earth.client.gui.widget.map.component.MarkerMapComponent;
import net.gegy1000.earth.server.world.pipeline.source.GoogleGeocoder;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;

public class EarthTeleportGui extends Gui {
    private static final int ACCEPT_BUTTON = 0;
    private static final int CANCEL_BUTTON = 1;
    private static final int SEARCH_FIELD = 2;

    private final double latitude;
    private final double longitude;

    private SlippyMapWidget mapWidget;
    private MarkerMapComponent markerComponent;
    private PlaceSearchWidget searchWidget;

    public EarthTeleportGui(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public void onInitialized() {
        if (this.mapWidget != null) {
            this.mapWidget.close();
        }

        this.client.keyboard.enableRepeatEvents(true);

        this.addButton(new ButtonWidget(ACCEPT_BUTTON, this.width / 2 - 155, this.height - 28, 150, 20, I18n.translate("gui.button.earth.teleport")) {
            @Override
            public void onPressed(double mouseX, double mouseY) {
                SlippyMapPoint marker = EarthTeleportGui.this.markerComponent.getMarker();
                EarthTeleportGui.this.method_2230(String.format("/geotp %s %s", marker.getLatitude(), marker.getLongitude()));
                EarthTeleportGui.this.client.openGui(null);
            }
        });
        this.addButton(new ButtonWidget(CANCEL_BUTTON, this.width / 2 + 5, this.height - 28, 150, 20, I18n.translate("gui.cancel")) {
            @Override
            public void onPressed(double mouseX, double mouseY) {
                EarthTeleportGui.this.client.openGui(null);
            }
        });

        this.mapWidget = new SlippyMapWidget(60, 20, this.width - 120, this.height - 80);
        this.mapWidget.getMap().focus(this.latitude, this.longitude, 5);

        this.markerComponent = new MarkerMapComponent(new SlippyMapPoint(this.latitude, this.longitude)).allowMovement();
        this.mapWidget.addComponent(this.markerComponent);

        this.searchWidget = new PlaceSearchWidget(SEARCH_FIELD, 65, 25, 180, 20, new GoogleGeocoder(), this::handleSearch);

        this.listeners.add(this.mapWidget);
        this.listeners.add(this.searchWidget);
    }

    private void handleSearch(double latitude, double longitude) {
        this.markerComponent.moveMarker(latitude, longitude);
        this.mapWidget.getMap().focus(latitude, longitude, 12);
    }

    @Override
    public void draw(int mouseX, int mouseY, float delta) {
        this.drawBackground();

        this.mapWidget.draw(mouseX, mouseY, delta);
        this.searchWidget.render(mouseX, mouseY, delta);

        this.drawStringCentered(this.fontRenderer, I18n.translate("gui.earth.teleport"), this.width / 2, 4, 0xFFFFFF);

        super.draw(mouseX, mouseY, delta);
    }

    @Override
    public void update() {
        super.update();
        this.searchWidget.tick();
    }

    @Override
    public void onClosed() {
        super.onClosed();
        this.mapWidget.close();
        this.searchWidget.onClosed();
        this.client.keyboard.enableRepeatEvents(false);
    }
}
