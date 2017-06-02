package net.gegy1000.terrarium.client.gui

import net.minecraft.client.gui.GuiScreen

class TrackerGUI : GuiScreen() {
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.drawDefaultBackground()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }
}
