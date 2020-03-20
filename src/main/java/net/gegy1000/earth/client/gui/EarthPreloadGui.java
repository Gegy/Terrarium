package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapPoint;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapWidget;
import net.gegy1000.earth.client.gui.widget.map.component.MarkerMapComponent;
import net.gegy1000.earth.client.gui.widget.map.component.RectMapComponent;
import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.message.StartDataDownloadMessage;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nullable;
import java.io.IOException;

public class EarthPreloadGui extends GuiScreen {
    private static final int CANCEL_BUTTON = 0;
    private static final int DOWNLOAD_BUTTON = 1;

    private final EarthWorld earth;
    private final double latitude;
    private final double longitude;

    private GuiButton downloadButton;

    private SlippyMapWidget mapWidget;
    private RectMapComponent mapSelection;

    public EarthPreloadGui(EarthWorld earth, double latitude, double longitude) {
        this.earth = earth;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public void initGui() {
        if (this.mapWidget != null) {
            this.mapWidget.close();
        }

        this.buttonList.clear();

        this.addButton(new GuiButton(CANCEL_BUTTON, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.cancel")));
        this.downloadButton = this.addButton(new GuiButton(DOWNLOAD_BUTTON, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.earth.download")));

        this.mapWidget = new SlippyMapWidget(60, 20, this.width - 120, this.height - 100);
        this.mapWidget.getMap().focus(0.0, 0.0, 3);

        this.mapSelection = this.mapWidget.addComponent(new RectMapComponent());

        this.mapWidget.addComponent(new MarkerMapComponent(new SlippyMapPoint(this.latitude, this.longitude)));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;

        if (button.id == CANCEL_BUTTON) {
            this.mc.displayGuiScreen(null);
        } else if (button.id == DOWNLOAD_BUTTON) {
            Tuple<ChunkPos, ChunkPos> selection = this.getSelection();
            if (selection == null) return;

            TerrariumEarth.NETWORK.sendToServer(new StartDataDownloadMessage(selection.getFirst(), selection.getSecond()));
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        this.mapWidget.draw(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, I18n.format("gui.earth.preload"), this.width / 2, 4, 0xFFFFFF);

        Tuple<ChunkPos, ChunkPos> selection = this.getSelection();
        if (selection != null) {
            ChunkPos minChunk = selection.getFirst();
            ChunkPos maxChunk = selection.getSecond();

            int width = maxChunk.x - minChunk.x + 1;
            int height = maxChunk.z - minChunk.z + 1;

            long chunkCount = (long) width * height;

            this.drawCenteredString(this.fontRenderer, width + "x" + height + " = " + chunkCount + " chunks", this.width / 2, this.height - 60, 0xFFFFFF);
        }

        this.downloadButton.enabled = selection != null;

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.mapWidget.mouseClicked(mouseX, mouseY, mouseButton);
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

    @Nullable
    private Tuple<ChunkPos, ChunkPos> getSelection() {
        RectMapComponent.Rect selection = this.mapSelection.getSelectedRect();
        if (selection != null) {
            CoordinateReference crs = this.earth.getCrs();
            Coordinate min = crs.coord(selection.minLongitude, selection.minLatitude).toBlock();
            Coordinate max = crs.coord(selection.maxLongitude, selection.maxLatitude).toBlock();

            return new Tuple<>(
                    new ChunkPos(Coordinate.min(min, max).toBlockPos()),
                    new ChunkPos(Coordinate.max(min, max).toBlockPos())
            );
        }

        return null;
    }
}
