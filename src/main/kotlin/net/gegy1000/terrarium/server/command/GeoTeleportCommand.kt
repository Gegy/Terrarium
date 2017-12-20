package net.gegy1000.terrarium.server.command

import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities
import net.gegy1000.terrarium.server.util.Coordinate
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentTranslation

class GeoTeleportCommand : CommandBase() {
    override fun getName() = "geotp"

    override fun getRequiredPermissionLevel() = 2

    override fun getUsage(sender: ICommandSender) = "commands.${Terrarium.MODID}:geotp.usage"

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        val player = CommandBase.getCommandSenderAsPlayer(sender)

        val terrariumData = player.world.getCapability(TerrariumCapabilities.worldDataCapability, null)
        if (terrariumData != null) {
            val settings = EarthGenerationSettings.deserialize(player.world.worldInfo.generatorOptions)

            val argument = args.joinToString(" ").replace(",", " ")
            val coordinateInput = argument.split(Regex("\\s+"))
            val coordinates = mutableListOf<Double>()

            for (coordinate in coordinateInput) {
                coordinates.add(coordinate.toDoubleOrNull() ?: continue)
            }

            if (coordinates.size == 2) {
                if (player is EntityPlayerMP) {
                    val latitude = coordinates[0]
                    val longitude = coordinates[1]

                    this.teleport(player, Coordinate.fromLatLng(settings, latitude, longitude))
                }
            } else if (args.isNotEmpty()) {
                if (player is EntityPlayerMP) {
                    val place = args.joinToString(" ")
                    val geocode = terrariumData.geocodingSource.get(place)

                    if (geocode != null) {
                        this.teleport(player, geocode)
                    } else {
                        throw WrongUsageException("commands.${Terrarium.MODID}:geotp.not_found", place)
                    }
                }
            } else {
                throw WrongUsageException(this.getUsage(sender))
            }
        } else {
            throw WrongUsageException("commands.${Terrarium.MODID}:geotp.wrong_world")
        }
    }

    private fun teleport(player: EntityPlayerMP, coordinate: Coordinate) {
        val blockX = coordinate.blockX.toInt()
        val blockZ = coordinate.blockZ.toInt()

        val chunk = player.world.getChunkFromChunkCoords(blockX shr 4, blockZ shr 4)
        val height = chunk.getHeightValue(blockX and 15, blockZ and 15)

        player.connection.setPlayerLocation(coordinate.blockX, height.toDouble(), coordinate.blockZ, 180.0F, 0.0F)
        player.sendMessage(TextComponentTranslation("commands.${Terrarium.MODID}:geotp.success", coordinate.latitude.toString(), coordinate.longitude.toString()))
    }
}
