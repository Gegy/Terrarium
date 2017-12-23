package net.gegy1000.terrarium.client.render;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TerrariumVertexFormats {
    public static final VertexFormat POSITION_COLOR_NORMAL = new VertexFormat();

    static {
        POSITION_COLOR_NORMAL.addElement(DefaultVertexFormats.POSITION_3F);
        POSITION_COLOR_NORMAL.addElement(DefaultVertexFormats.COLOR_4UB);
        POSITION_COLOR_NORMAL.addElement(DefaultVertexFormats.NORMAL_3B);
        POSITION_COLOR_NORMAL.addElement(DefaultVertexFormats.PADDING_1B);
    }
}
