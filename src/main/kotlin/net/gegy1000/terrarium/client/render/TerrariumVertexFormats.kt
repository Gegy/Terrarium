package net.gegy1000.terrarium.client.render

import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.vertex.VertexFormat

object TerrariumVertexFormats {
    val POSITION_COLOR_NORMAL = VertexFormat()

    init {
        POSITION_COLOR_NORMAL.addElement(DefaultVertexFormats.POSITION_3F)
        POSITION_COLOR_NORMAL.addElement(DefaultVertexFormats.COLOR_4UB)
        POSITION_COLOR_NORMAL.addElement(DefaultVertexFormats.NORMAL_3B)
        POSITION_COLOR_NORMAL.addElement(DefaultVertexFormats.PADDING_1B)
    }
}
