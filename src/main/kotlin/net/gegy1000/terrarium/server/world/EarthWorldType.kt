package net.gegy1000.terrarium.server.world

import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.client.gui.CustomizeEarthGUI
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities
import net.gegy1000.terrarium.server.world.generator.EarthBiomeProvider
import net.gegy1000.terrarium.server.world.generator.EarthChunkGenerator
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiCreateWorld
import net.minecraft.world.World
import net.minecraft.world.WorldType
import net.minecraft.world.biome.BiomeProvider
import net.minecraft.world.gen.IChunkGenerator
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

object EarthWorldType : WorldType("${Terrarium.MODID}:earth") {
    override fun getChunkGenerator(world: World, settings: String): IChunkGenerator {
        return EarthChunkGenerator(world, world.seed, settings)
    }

    override fun getBiomeProvider(world: World): BiomeProvider {
        return EarthBiomeProvider(world)
    }

    @SideOnly(Side.CLIENT)
    override fun onCustomizeButton(mc: Minecraft, parent: GuiCreateWorld) {
        mc.displayGuiScreen(CustomizeEarthGUI(parent))
    }

    override fun isCustomizable() = true
}
