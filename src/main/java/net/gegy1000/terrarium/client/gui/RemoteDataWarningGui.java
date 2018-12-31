package net.gegy1000.terrarium.client.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.MainMenuGui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.text.TextFormat;

public class RemoteDataWarningGui extends Gui {
    private static final int ACCEPT_ID = 0;
    private static final int CANCEL_ID = 1;

    private Gui parent;
    private boolean complete;

    public RemoteDataWarningGui(Gui parent) {
        super();
        this.parent = parent;
    }

    @Override
    public void onInitialized() {
        this.addButton(new ButtonWidget(ACCEPT_ID, this.width / 2 - 155, this.height - 28, 150, 20, I18n.translate("gui.terrarium.accept")) {
            @Override
            public void onPressed(double mouseX, double mouseY) {
                RemoteDataWarningGui.this.complete = true;
                RemoteDataWarningGui.this.client.openGui(RemoteDataWarningGui.this.parent);
            }
        });
        this.addButton(new ButtonWidget(CANCEL_ID, this.width / 2 + 5, this.height - 28, 150, 20, I18n.translate("gui.cancel")) {
            @Override
            public void onPressed(double mouseX, double mouseY) {
                RemoteDataWarningGui.this.complete = true;

                this.enabled = false;

                RemoteDataWarningGui.this.client.world.method_8525();
                RemoteDataWarningGui.this.client.method_1481(null);

                if (RemoteDataWarningGui.this.client.isConnectedToRealms()) {
                    new RealmsBridge().switchToRealms(new MainMenuGui());
                } else {
                    RemoteDataWarningGui.this.client.openGui(new MainMenuGui());
                }
            }
        });
    }

    public void setParent(Gui parent) {
        this.parent = parent;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        this.drawBackground();

        int warningX = this.width / 2;
        this.drawStringCentered(this.fontRenderer, TextFormat.RED.toString() + TextFormat.BOLD + I18n.translate("gui.terrarium.remote_data_warning.1"), warningX, this.height / 2 - 24, 0xFFFFFF);

        this.drawStringCentered(this.fontRenderer, TextFormat.ITALIC + I18n.translate("gui.terrarium.remote_data_warning.2"), warningX, this.height / 2 - 9, 0xFFFFFF);
        this.drawStringCentered(this.fontRenderer, TextFormat.ITALIC + I18n.translate("gui.terrarium.remote_data_warning.3"), warningX, this.height / 2, 0xFFFFFF);
        this.drawStringCentered(this.fontRenderer, TextFormat.ITALIC + I18n.translate("gui.terrarium.remote_data_warning.4"), warningX, this.height / 2 + 9, 0xFFFFFF);
        this.drawStringCentered(this.fontRenderer, TextFormat.BOLD + I18n.translate("gui.terrarium.remote_data_warning.5"), warningX, this.height / 2 + 18, 0xFFFFFF);

        super.draw(mouseX, mouseY, partialTicks);
    }

    public boolean isComplete() {
        return this.complete;
    }
}
