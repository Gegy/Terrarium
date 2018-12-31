package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.client.gui.widget.map.SlippyMapPoint;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapWidget;
import net.gegy1000.earth.client.gui.widget.map.component.MarkerMapComponent;
import net.gegy1000.terrarium.client.gui.widget.CopyBoxWidget;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;

public class EarthLocateGui extends Gui {
    private final double latitude;
    private final double longitude;

    private SlippyMapWidget mapWidget;
    private MarkerMapComponent markerComponent;

    private CopyBoxWidget locationBox;

    public EarthLocateGui(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public void onInitialized() {
        if (this.mapWidget != null) {
            this.mapWidget.close();
        }

        this.addButton(new ButtonWidget(0, (this.width - 200) / 2, this.height - 30, 200, 20, I18n.translate("gui.done")) {
            @Override
            public void onPressed(double mouseX, double mouseY) {
                EarthLocateGui.this.client.openGui(null);
            }
        });

        this.mapWidget = new SlippyMapWidget(60, 20, this.width - 120, this.height - 100);
        this.mapWidget.getMap().focus(this.latitude, this.longitude, 5);

        this.markerComponent = new MarkerMapComponent(new SlippyMapPoint(this.latitude, this.longitude));
        this.mapWidget.addComponent(this.markerComponent);

        int copyBoxWidth = 240;
        String locationText = String.format("%.5f, %.5f", this.latitude, this.longitude);
        this.locationBox = new CopyBoxWidget((this.width - copyBoxWidth) / 2, this.height - 75, copyBoxWidth, 20, locationText, this.fontRenderer);

        this.listeners.add(this.locationBox);
        this.listeners.add(this.mapWidget);
    }

    @Override
    public void draw(int mouseX, int mouseY, float delta) {
        this.drawBackground();

        this.mapWidget.draw(mouseX, mouseY, delta);
        this.drawStringCentered(this.fontRenderer, I18n.translate("gui.earth.locate"), this.width / 2, 4, 0xFFFFFF);

        this.locationBox.draw(mouseX, mouseY);

        super.draw(mouseX, mouseY, delta);
    }

    @Override
    public void onClosed() {
        super.onClosed();
        this.mapWidget.close();
    }
}
