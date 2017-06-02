package net.gegy1000.terrarium.server.item

import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.map.source.GlobTilePos
import net.gegy1000.terrarium.server.map.source.GlobcoverSource
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.gegy1000.terrarium.server.world.EarthWorldType
import net.gegy1000.terrarium.server.world.generator.EarthGenerationHandler
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumHand
import net.minecraft.util.math.MathHelper
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.translation.I18n
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class TrackerItem : Item() {
    init {
        this.unlocalizedName = "${Terrarium.MODID}:tracker"
        this.creativeTab = CreativeTabs.TRANSPORTATION
    }

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, player: EntityPlayer, tooltip: MutableList<String>, advanced: Boolean) {
        if (player.world.worldType !is EarthWorldType) {
            tooltip.add(TextFormatting.RED.toString() + I18n.translateToLocal("tooltip.${Terrarium.MODID}:tracker_no_signal.name"))
        } else {
            val settings = EarthGenerationSettings.deserialize(player.world.worldInfo.generatorOptions)
            val globScaledX = (player.posX * EarthGenerationHandler.GLOB_SCALE_X)
            val globScaledZ = (player.posZ * EarthGenerationHandler.GLOB_SCALE_Y)

            val scaledWidth = (EarthGenerationHandler.WIDTH * settings.scale * EarthGenerationHandler.REAL_SCALE).toInt()
            val scaledHeight = (EarthGenerationHandler.HEIGHT * settings.scale * EarthGenerationHandler.REAL_SCALE).toInt()

            val scaledX = globScaledX / (scaledWidth - 1) * (EarthGenerationHandler.WIDTH - 1)
            val scaledZ = globScaledZ / (scaledHeight - 1) * (EarthGenerationHandler.HEIGHT - 1)

            val roundX = scaledX.toInt()
            val roundZ = scaledZ.toInt()

            tooltip.add(roundX.toString())
            tooltip.add(roundZ.toString())
            val pos = GlobTilePos(MathHelper.intFloorDiv(roundX, GlobcoverSource.REGION_TILE), MathHelper.intFloorDiv(roundZ, GlobcoverSource.REGION_TILE))
            val localX = (roundX - pos.minX) / 10
            val localZ = (roundZ - pos.minZ) / 10
            tooltip.add(pos.name)
            tooltip.add("$localX, $localZ")
            tooltip.add(GlobcoverSource[roundX, roundZ].name)
        }
    }

    override fun onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult<ItemStack> {
        if (player.world.worldType is EarthWorldType) {
            return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand))
        } else {
            if (player.world.isRemote) {
                val style = Style().setColor(TextFormatting.RED)
                val message = TextComponentTranslation("tooltip.${Terrarium.MODID}:tracker_no_signal.name").setStyle(style)
                player.sendStatusMessage(message, true)
            }
            return super.onItemRightClick(world, player, hand)
        }
    }
}