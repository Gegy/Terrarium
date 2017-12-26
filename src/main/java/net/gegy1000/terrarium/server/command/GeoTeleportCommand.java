package net.gegy1000.terrarium.server.command;

import joptsimple.internal.Strings;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.chunk.Chunk;

import java.io.IOException;

public class GeoTeleportCommand extends CommandBase {
    @Override
    public String getName() {
        return "geotp";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.terrarium.geotp.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = CommandBase.getCommandSenderAsPlayer(sender);

        TerrariumWorldData worldData = player.world.getCapability(TerrariumCapabilities.worldDataCapability, null);
        if (worldData != null) {
            EarthGenerationSettings settings = EarthGenerationSettings.deserialize(player.world.getWorldInfo().getGeneratorOptions());

            String argument = String.join(" ", args).replace(',', ' ');
            String[] locationInput = argument.split("\\s+");

            // TODO: Don't block the server while loading generation region and geocode
            CommandLocation location = this.parseLocation(sender, locationInput);
            this.teleport(player, location.getCoordinate(worldData, settings));
        } else {
            throw new WrongUsageException("commands.terrarium.geotp.wrong_world");
        }
    }

    private CommandLocation parseLocation(ICommandSender sender, String[] input) throws WrongUsageException {
        if (input.length == 2) {
            boolean valid = true;
            double[] coordinates = new double[2];
            for (int i = 0; i < 2; i++) {
                try {
                    coordinates[i] = Double.parseDouble(input[i]);
                } catch (NumberFormatException e) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                return new CoordinateLocation(coordinates[0], coordinates[1]);
            }
        }

        String place = String.join(" ", input);
        if (Strings.isNullOrEmpty(place)) {
            throw new WrongUsageException(this.getUsage(sender));
        }

        return new GeocodeLocation(place);
    }

    private void teleport(EntityPlayerMP player, Coordinate coordinate) {
        int blockX = MathHelper.floor(coordinate.getBlockX());
        int blockZ = MathHelper.floor(coordinate.getBlockZ());

        Chunk chunk = player.world.getChunkFromChunkCoords(blockX >> 4, blockZ >> 4);
        int height = chunk.getHeightValue(blockX & 15, blockZ & 15);

        player.connection.setPlayerLocation(coordinate.getBlockX(), height + 0.5, coordinate.getBlockZ(), 180.0F, 0.0F);
        player.sendMessage(new TextComponentTranslation("commands.terrarium.geotp.success", coordinate.getLatitude(), coordinate.getLongitude()));
    }

    private interface CommandLocation {
        Coordinate getCoordinate(TerrariumWorldData worldData, EarthGenerationSettings settings) throws CommandException;
    }

    private class CoordinateLocation implements CommandLocation {
        private final double latitude;
        private final double longitude;

        private CoordinateLocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public Coordinate getCoordinate(TerrariumWorldData worldData, EarthGenerationSettings settings) {
            return Coordinate.fromLatLng(settings, this.latitude, this.longitude);
        }
    }

    private class GeocodeLocation implements CommandLocation {
        private final String place;

        private GeocodeLocation(String place) {
            this.place = place;
        }

        @Override
        public Coordinate getCoordinate(TerrariumWorldData worldData, EarthGenerationSettings settings) throws CommandException {
            try {
                Coordinate geocode = worldData.getGeocodingSource().get(this.place);
                if (geocode == null) {
                    throw new WrongUsageException("commands.terrarium.geotp.not_found", this.place);
                }
                return geocode;
            } catch (IOException e) {
                Terrarium.LOGGER.error("Failed to get geocode for {}", this.place, e);
                throw new WrongUsageException("commands.terrarium.geotp.error", this.place, e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
