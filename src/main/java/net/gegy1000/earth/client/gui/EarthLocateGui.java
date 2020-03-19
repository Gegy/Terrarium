package net.gegy1000.earth.client.gui;

import net.gegy1000.terrarium.client.gui.widget.CopyBoxWidget;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapPoint;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapWidget;
import net.gegy1000.earth.client.gui.widget.map.component.MarkerMapComponent;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

public class EarthLocateGui extends GuiScreen {
    private final double latitude;
    private final double longitude;

    private SlippyMapWidget mapWidget;

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
        this.buttonList.add(new GuiButton(0, (this.width - 200) / 2, this.height - 30, 200, 20, I18n.format("gui.done")));

        this.mapWidget = new SlippyMapWidget(60, 20, this.width - 120, this.height - 100);
        this.mapWidget.getMap().focus(this.latitude, this.longitude, 11);

        this.mapWidget.addComponent(new MarkerMapComponent(new SlippyMapPoint(this.latitude, this.longitude)));

        int copyBoxWidth = 240;
        String locationText = String.format("%.5f, %.5f", this.latitude, this.longitude);
        this.locationBox = new CopyBoxWidget((this.width - copyBoxWidth) / 2, this.height - 75, copyBoxWidth, 20, locationText, this.fontRenderer);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.visible) {
            if (button.id == 0) {
                this.mc.displayGuiScreen(null);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        this.mapWidget.draw(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, I18n.format("gui.earth.locate"), this.width / 2, 4, 0xFFFFFF);

        this.locationBox.draw(mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
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
    }
}
