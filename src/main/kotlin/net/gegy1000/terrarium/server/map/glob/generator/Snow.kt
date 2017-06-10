package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobGenerator
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import java.util.Random

class Snow : GlobGenerator(GlobType.SNOW) {
    companion object {
        val SNOW: IBlockState = Blocks.SNOW.defaultState
    }

    override fun getCoverAt(x: Int, z: Int, random: Random) = SNOW

    override fun getFillerAt(x: Int, z: Int, random: Random) = SNOW
}
