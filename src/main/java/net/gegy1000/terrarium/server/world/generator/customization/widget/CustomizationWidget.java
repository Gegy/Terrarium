package net.gegy1000.terrarium.server.world.generator.customization.widget;

import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface CustomizationWidget {
    @SideOnly(Side.CLIENT)
    GuiButton createWidget(GenerationSettings settings, Runnable onPropertyChange);
}
