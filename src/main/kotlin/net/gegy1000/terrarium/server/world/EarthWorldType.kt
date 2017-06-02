package net.gegy1000.terrarium.server.world

import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.client.gui.CustomizeEarthGUI
import net.gegy1000.terrarium.server.world.generator.EarthBiomeProvider
import net.gegy1000.terrarium.server.world.generator.EarthChunkGenerator
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiCreateWorld
import net.minecraft.world.World
import net.minecraft.world.WorldType
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class EarthWorldType : WorldType("${Terrarium.MODID}:earth") {
    companion object {
        val INSTANCE = EarthWorldType()
    }

    override fun getChunkGenerator(world: World, settings: String) = EarthChunkGenerator(world, world.seed, settings)

    override fun getBiomeProvider(world: World) = EarthBiomeProvider(EarthGenerationSettings.deserialize(world.worldInfo.generatorOptions))

    @SideOnly(Side.CLIENT)
    override fun onCustomizeButton(mc: Minecraft, parent: GuiCreateWorld) {
        mc.displayGuiScreen(CustomizeEarthGUI(parent))
    }

    override fun isCustomizable() = true
}
