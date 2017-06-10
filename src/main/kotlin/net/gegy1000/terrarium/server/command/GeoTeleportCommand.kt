package net.gegy1000.terrarium.server.command

import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.map.source.GeocodingSource
import net.gegy1000.terrarium.server.util.Coordinates
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.gegy1000.terrarium.server.world.EarthWorldType
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

        if (player.world.worldType is EarthWorldType) {
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

                    this.teleport(player, latitude, longitude)
                }
            } else if (args.isNotEmpty()) {
                if (player is EntityPlayerMP) {
                    if (player.world.worldType is EarthWorldType) {
                        val place = args.joinToString(" ")
                        val geocode = GeocodingSource[place]

                        if (geocode != null) {
                            this.teleport(player, geocode.latitude, geocode.longitude)
                        } else {
                            throw WrongUsageException("commands.${Terrarium.MODID}:geotp.not_found", place)
                        }
                    }
                }
            } else {
                throw WrongUsageException(this.getUsage(sender))
            }
        } else {
            throw WrongUsageException("commands.${Terrarium.MODID}:geotp.wrong_world")
        }
    }

    private fun teleport(player: EntityPlayerMP, latitude: Double, longitude: Double) {
        val settings = EarthGenerationSettings.deserialize(player.world.worldInfo.generatorOptions)

        val x = Coordinates.fromLongitude(longitude, settings)
        val z = Coordinates.fromLatitude(latitude, settings)

        val blockX = x.toInt()
        val blockZ = z.toInt()

        val chunk = player.world.getChunkFromChunkCoords(blockX shr 4, blockZ shr 4)
        val height = chunk.getHeightValue(blockX and 15, blockZ and 15)

        player.connection.setPlayerLocation(x, height.toDouble(), z, 180.0F, 0.0F)
        player.sendMessage(TextComponentTranslation("commands.${Terrarium.MODID}:geotp.success", latitude.toString(), longitude.toString()))
    }
}