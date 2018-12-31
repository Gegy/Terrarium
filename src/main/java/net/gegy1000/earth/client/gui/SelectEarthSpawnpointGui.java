package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.client.gui.widget.map.PlaceSearchWidget;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapPoint;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapWidget;
import net.gegy1000.earth.client.gui.widget.map.component.MarkerMapComponent;
import net.gegy1000.earth.server.world.EarthGeneratorType;
import net.gegy1000.earth.server.world.pipeline.source.GoogleGeocoder;
import net.gegy1000.terrarium.server.world.customization.GenerationSettings;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;

public class SelectEarthSpawnpointGui extends Gui {
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
    public void onInitialized() {
        if (this.mapWidget != null) {
            this.mapWidget.close();
        }

        this.client.keyboard.enableRepeatEvents(true);

        this.mapWidget = new SlippyMapWidget(20, 20, this.width - 40, this.height - 60);

        GenerationSettings settings = this.parent.getSettings();

        double latitude = settings.getDouble(EarthGeneratorType.SPAWN_LATITUDE);
        double longitude = settings.getDouble(EarthGeneratorType.SPAWN_LONGITUDE);
        this.markerComponent = new MarkerMapComponent(new SlippyMapPoint(latitude, longitude)).allowMovement();
        this.mapWidget.addComponent(this.markerComponent);

        this.searchWidget = new PlaceSearchWidget(SEARCH_FIELD, 25, 25, 200, 20, new GoogleGeocoder(), this::handleSearch);
        this.searchWidget.setHasFocus(true);

        this.listeners.add(this.searchWidget);
        this.listeners.add(this.mapWidget);

        this.addButton(new ButtonWidget(SELECT_BUTTON, this.width / 2 - 154, this.height - 28, 150, 20, I18n.translate("gui.done")) {
            @Override
            public void onPressed(double mouseX, double mouseY) {
                SelectEarthSpawnpointGui.this.client.openGui(SelectEarthSpawnpointGui.this.parent);
                SlippyMapPoint marker = SelectEarthSpawnpointGui.this.markerComponent.getMarker();
                if (marker != null) {
                    SelectEarthSpawnpointGui.this.parent.applySpawnpoint(marker.getLatitude(), marker.getLongitude());
                }
            }
        });
        this.addButton(new ButtonWidget(CANCEL_BUTTON, this.width / 2 + 4, this.height - 28, 150, 20, I18n.translate("gui.cancel")) {
            @Override
            public void onPressed(double mouseX, double mouseY) {
                SelectEarthSpawnpointGui.this.client.openGui(SelectEarthSpawnpointGui.this.parent);
            }
        });
    }

    private void handleSearch(double latitude, double longitude) {
        this.markerComponent.moveMarker(latitude, longitude);
        this.mapWidget.getMap().focus(latitude, longitude, 12);
    }

    @Override
    public void draw(int mouseX, int mouseY, float delta) {
        super.drawBackground();
        this.mapWidget.draw(mouseX, mouseY, delta);
        this.searchWidget.render(mouseX, mouseY, delta);
        this.drawStringCentered(this.fontRenderer, I18n.translate("gui.earth.spawnpoint"), this.width / 2, 4, 0xFFFFFF);
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
