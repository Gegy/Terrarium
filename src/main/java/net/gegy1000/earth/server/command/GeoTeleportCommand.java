package net.gegy1000.earth.server.command;

import com.google.common.base.Strings;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.capability.EarthCapability;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import javax.vecmath.Vector2d;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
        return DeferredTranslator.translateString(sender, "commands.earth.geotp.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = CommandBase.getCommandSenderAsPlayer(sender);

        EarthCapability earthData = player.world.getCapability(TerrariumEarth.earthCap, null);
        if (earthData != null) {
            String argument = String.join(" ", args).replace(',', ' ');
            String[] locationInput = argument.split("\\s+");

            // TODO: Don't block the server while loading generation region and geocode
            CommandLocation location = this.parseLocation(sender, locationInput);
            this.teleport(player, location.getCoordinate(sender, earthData));
        } else {
            throw DeferredTranslator.createException(player, "commands.earth.wrong_world");
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;

            EarthCapability earthData = player.world.getCapability(TerrariumEarth.earthCap, null);
            if (earthData != null) {
                String argument = String.join(" ", args);
                try {
                    return getListOfStringsMatchingLastWord(args, earthData.getGeocoder().suggestCommand(argument));
                } catch (IOException e) {
                    Terrarium.LOGGER.warn("Failed to get geotp suggestions", e);
                }
            }
        }

        return Collections.emptyList();
    }

    private CommandLocation parseLocation(ICommandSender sender, String[] input) throws WrongUsageException {
        CommandLocation location = this.parseCoordinateLocation(input);

        if (location == null) {
            String place = String.join(" ", input);
            if (Strings.isNullOrEmpty(place)) {
                throw new WrongUsageException(this.getUsage(sender));
            }
            location = new GeocodeLocation(place);
        }

        return location;
    }

    private CommandLocation parseCoordinateLocation(String[] input) {
        if (input.length == 2) {
            double[] coordinates = new double[2];
            for (int i = 0; i < 2; i++) {
                try {
                    coordinates[i] = Double.parseDouble(input[i]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return new CoordinateLocation(coordinates[0], coordinates[1]);
        }
        return null;
    }

    private void teleport(EntityPlayerMP player, Coordinate coordinate) {
        int blockX = MathHelper.floor(coordinate.getBlockX());
        int blockZ = MathHelper.floor(coordinate.getBlockZ());

        Chunk chunk = player.world.getChunkFromChunkCoords(blockX >> 4, blockZ >> 4);
        int height = chunk.getHeightValue(blockX & 15, blockZ & 15);

        player.connection.setPlayerLocation(coordinate.getBlockX(), height + 0.5, coordinate.getBlockZ(), 180.0F, 0.0F);
        player.sendMessage(DeferredTranslator.translate(player, new TextComponentTranslation("commands.earth.geotp.success", coordinate.getX(), coordinate.getZ())));
    }

    private interface CommandLocation {
        Coordinate getCoordinate(ICommandSender sender, EarthCapability worldData) throws CommandException;
    }

    private class CoordinateLocation implements CommandLocation {
        private final double latitude;
        private final double longitude;

        private CoordinateLocation(double latitude, double z) {
            this.latitude = latitude;
            this.longitude = z;
        }

        @Override
        public Coordinate getCoordinate(ICommandSender sender, EarthCapability worldData) {
            return new Coordinate(worldData.getGeoCoordinate(), this.latitude, this.longitude);
        }
    }

    private class GeocodeLocation implements CommandLocation {
        private final String place;

        private GeocodeLocation(String place) {
            this.place = place;
        }

        @Override
        public Coordinate getCoordinate(ICommandSender sender, EarthCapability worldData) throws CommandException {
            try {
                Vector2d coordinate = worldData.getGeocoder().get(this.place);
                if (coordinate == null) {
                    throw DeferredTranslator.createException(sender, "commands.earth.geotp.not_found", this.place);
                }

                return new Coordinate(worldData.getGeoCoordinate(), coordinate.getX(), coordinate.getY());
            } catch (IOException e) {
                Terrarium.LOGGER.error("Failed to get geocode for {}", this.place, e);
                throw DeferredTranslator.createException(sender, "commands.earth.geotp.error", this.place, e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
