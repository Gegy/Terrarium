package net.gegy1000.terrarium.client.gui;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.config.TerrariumConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
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
        this.addButton(new GuiButton(ACCEPT_ID, this.width / 2 - 155, this.height - 28, 150, 20, I18n.translateToLocal("gui.terrarium.accept")));
        this.addButton(new GuiButton(CANCEL_ID, this.width / 2 + 5, this.height - 28, 150, 20, I18n.translateToLocal("gui.cancel")));
    }

    public void setParent(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == ACCEPT_ID) {
                this.complete = true;

                TerrariumConfig.acceptedRemoteDataWarning = true;
                ConfigManager.sync(Terrarium.MODID, Config.Type.INSTANCE);

                this.mc.displayGuiScreen(this.parent);
            } else if (button.id == CANCEL_ID) {
                this.complete = true;

                button.enabled = false;

                this.mc.world.sendQuittingDisconnectingPacket();
                this.mc.loadWorld(null);

                if (this.mc.isConnectedToRealms()) {
                    new RealmsBridge().switchToRealms(new GuiMainMenu());
                } else {
                    this.mc.displayGuiScreen(new GuiMainMenu());
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        this.drawCenteredString(this.fontRenderer, TextFormatting.RED.toString() + TextFormatting.BOLD + I18n.translateToLocal("gui.terrarium.remote_data_warning.1"), this.width / 2, this.height / 2 - 24, 0xFFFFFF);

        this.drawCenteredString(this.fontRenderer, TextFormatting.ITALIC + I18n.translateToLocal("gui.terrarium.remote_data_warning.2"), this.width / 2, this.height / 2 - 9, 0xFFFFFF);
        this.drawCenteredString(this.fontRenderer, TextFormatting.ITALIC + I18n.translateToLocal("gui.terrarium.remote_data_warning.3"), this.width / 2, this.height / 2, 0xFFFFFF);
        this.drawCenteredString(this.fontRenderer, TextFormatting.ITALIC + I18n.translateToLocal("gui.terrarium.remote_data_warning.4"), this.width / 2, this.height / 2 + 9, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public boolean isComplete() {
        return this.complete;
    }
}
