package net.gegy1000.earth.server.command;

import com.google.common.base.Strings;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.vecmath.Vector2d;
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
        return DeferredTranslator.translateString(sender, "commands.earth.geotp.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        Entity entity = getTeleportedEntity(server, sender, args);

        EarthWorld earth = entity.world.getCapability(TerrariumEarth.worldCap(), null);
        if (earth != null) {
            String argument = String.join(" ", args).replace(',', ' ');
            String[] locationInput = argument.split("\\s+");

            Thread thread = new Thread(() -> {
                try {
                    CommandLocation location = this.parseLocation(sender, locationInput);
                    Coordinate coordinate = location.getCoordinate(sender, earth);
                    server.addScheduledTask(() -> this.teleport(entity, earth, coordinate));
                } catch (CommandException e) {
                    TextComponentTranslation message = new TextComponentTranslation(e.getMessage(), e.getErrorObjects());
                    message.getStyle().setColor(TextFormatting.RED);
                    sender.sendMessage(message);
                }
            });
            thread.setDaemon(true);
            thread.start();
        } else {
            throw DeferredTranslator.createException(entity, "commands.earth.wrong_world");
        }
    }

    private static Entity getTeleportedEntity(MinecraftServer server, ICommandSender sender, String[] args) throws EntityNotFoundException {
        EntityPlayerMP player = getPlayerSender(sender);

        if (args.length == 3) {
            try {
                return getEntity(server, sender, args[0]);
            } catch (CommandException e) {
                // If we failed to parse an entity selector, it's probably not an entity selector. Let's use the sender
            }
        }

        if (player == null) {
            throw new EntityNotFoundException("Not player or no entity selector given");
        }

        return player;
    }

    private static EntityPlayerMP getPlayerSender(ICommandSender sender) {
        try {
            return getCommandSenderAsPlayer(sender);
        } catch (CommandException e) {
            return null;
        }
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

    private void teleport(Entity entity, EarthWorld earthData, Coordinate coordinate) {
        int blockX = MathHelper.floor(coordinate.getBlockX());
        int blockZ = MathHelper.floor(coordinate.getBlockZ());

        int height = this.getHeight(entity.world, earthData, blockX, blockZ);

        entity.dismountRidingEntity();

        entity.lastTickPosX = entity.posX;
        entity.lastTickPosY = entity.posY;
        entity.lastTickPosZ = entity.posZ;

        entity.motionX = 0.0;
        entity.motionY = 0.0;
        entity.motionZ = 0.0;

        entity.onGround = true;

        if (entity instanceof EntityPlayerMP) {
            NetHandlerPlayServer connection = ((EntityPlayerMP) entity).connection;
            connection.setPlayerLocation(coordinate.getBlockX(), height + 0.5, coordinate.getBlockZ(), 180.0F, 0.0F);
        }

        entity.sendMessage(DeferredTranslator.translate(entity, new TextComponentTranslation("commands.earth.geotp.success", coordinate.getX(), coordinate.getZ())));
    }

    private int getHeight(World world, EarthWorld earthData, int x, int z) {
        BlockPos surface = earthData.estimateSurface(world, x, z);
        if (surface != null) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(surface);
            while (world.getBlockState(pos).getMaterial().blocksMovement()) {
                pos.move(EnumFacing.UP);
            }
            return pos.getY();
        }

        return world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
    }

    private interface CommandLocation {
        Coordinate getCoordinate(ICommandSender sender, EarthWorld worldData) throws CommandException;
    }

    private class CoordinateLocation implements CommandLocation {
        private final double latitude;
        private final double longitude;

        private CoordinateLocation(double latitude, double z) {
            this.latitude = latitude;
            this.longitude = z;
        }

        @Override
        public Coordinate getCoordinate(ICommandSender sender, EarthWorld worldData) {
            return new Coordinate(worldData.getCrs(), this.latitude, this.longitude);
        }
    }

    private class GeocodeLocation implements CommandLocation {
        private final String place;

        private GeocodeLocation(String place) {
            this.place = place;
        }

        @Override
        public Coordinate getCoordinate(ICommandSender sender, EarthWorld worldData) throws CommandException {
            try {
                Vector2d coordinate = worldData.getGeocoder().get(this.place);
                if (coordinate == null) {
                    throw DeferredTranslator.createException(sender, "commands.earth.geotp.not_found", this.place);
                }

                return new Coordinate(worldData.getCrs(), coordinate.getX(), coordinate.getY());
            } catch (IOException e) {
                Terrarium.LOGGER.error("Failed to get geocode for {}", this.place, e);
                throw DeferredTranslator.createException(sender, "commands.earth.geotp.error", this.place, e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
