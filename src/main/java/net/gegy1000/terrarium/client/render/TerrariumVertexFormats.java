package net.gegy1000.terrarium.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

@Environment(EnvType.CLIENT)
public class TerrariumVertexFormats {
    public static final VertexFormat POSITION_COLOR_NORMAL = new VertexFormat();

    static {
        POSITION_COLOR_NORMAL.add(VertexFormats.POSITION_ELEMENT);
        POSITION_COLOR_NORMAL.add(VertexFormats.COLOR_ELEMENT);
        POSITION_COLOR_NORMAL.add(VertexFormats.NORMAL_ELEMENT);
        POSITION_COLOR_NORMAL.add(VertexFormats.PADDING_ELEMENT);
    }
}
