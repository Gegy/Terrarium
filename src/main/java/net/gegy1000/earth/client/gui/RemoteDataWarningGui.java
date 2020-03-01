package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.config.TerrariumEarthConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

public class RemoteDataWarningGui extends GuiScreen {
    private static final int ACCEPT_ID = 0;
    private static final int CANCEL_ID = 1;

    private GuiScreen parent;
    private boolean complete;

    public RemoteDataWarningGui(GuiScreen parent) {
        super();
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.addButton(new GuiButton(ACCEPT_ID, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.terrarium.accept")));
        this.addButton(new GuiButton(CANCEL_ID, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));
    }

    public void setParent(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == ACCEPT_ID) {
                this.complete = true;

                TerrariumEarthConfig.acceptedRemoteDataWarning = true;
                ConfigManager.sync(TerrariumEarth.ID, Config.Type.INSTANCE);

                this.mc.displayGuiScreen(this.parent);
            } else if (button.id == CANCEL_ID) {
                this.complete = true;

                button.enabled = false;

                if (this.mc.world != null) {
                    this.mc.world.sendQuittingDisconnectingPacket();
                    this.mc.loadWorld(null);
                }

                this.mc.displayGuiScreen(new GuiMainMenu());
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int warningX = this.width / 2;
        this.drawCenteredString(this.fontRenderer, TextFormatting.RED.toString() + TextFormatting.BOLD + I18n.format("gui.terrarium.remote_data_warning.1"), warningX, this.height / 2 - 24, 0xFFFFFF);

        this.drawCenteredString(this.fontRenderer, TextFormatting.ITALIC + I18n.format("gui.terrarium.remote_data_warning.2"), warningX, this.height / 2 - 9, 0xFFFFFF);
        this.drawCenteredString(this.fontRenderer, TextFormatting.ITALIC + I18n.format("gui.terrarium.remote_data_warning.3"), warningX, this.height / 2, 0xFFFFFF);
        this.drawCenteredString(this.fontRenderer, TextFormatting.ITALIC + I18n.format("gui.terrarium.remote_data_warning.4"), warningX, this.height / 2 + 9, 0xFFFFFF);
        this.drawCenteredString(this.fontRenderer, TextFormatting.BOLD + I18n.format("gui.terrarium.remote_data_warning.5"), warningX, this.height / 2 + 18, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public boolean isComplete() {
        return this.complete;
    }
}
