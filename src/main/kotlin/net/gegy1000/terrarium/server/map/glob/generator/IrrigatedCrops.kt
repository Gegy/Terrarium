package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobType
import net.minecraft.block.state.IBlockState
import java.util.Random

class IrrigatedCrops : Cropland(GlobType.IRRIGATED_CROPS) {
    override fun getCover(x: Int, z: Int, random: Random): IBlockState {
        if (x % 9 == 0 && z % 9 == 0) {
            return WATER
        }
        if (random.nextInt(60) == 0) {
            return COARSE_DIRT
        }
        return FARMLAND
    }
}
