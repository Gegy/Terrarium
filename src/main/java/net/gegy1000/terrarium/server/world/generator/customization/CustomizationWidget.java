package net.gegy1000.terrarium.server.world.generator.customization;

import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface CustomizationWidget {
    @SideOnly(Side.CLIENT)
    GuiButton createWidget(GenerationSettings settings, int id, int x, int y);
}
