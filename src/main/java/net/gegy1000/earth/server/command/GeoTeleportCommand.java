package net.gegy1000.earth.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.gegy1000.earth.server.world.EarthGeneratorConfig;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.util.Point2d;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.minecraft.command.arguments.Vec2ArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TextFormatter;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;

import java.io.IOException;

import static net.minecraft.server.command.ServerCommandManager.argument;
import static net.minecraft.server.command.ServerCommandManager.literal;

public class GeoTeleportCommand {
    private static final SimpleCommandExceptionType WRONG_WORLD = new SimpleCommandExceptionType(
            new TranslatableTextComponent("commands.earth.wrong_world")
    );
    private static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType((place) -> {
        return new TranslatableTextComponent("commands.earth.geotp.not_found", place);
    });
    private static final Dynamic3CommandExceptionType GEOCODE_ERROR = new Dynamic3CommandExceptionType((place, type, message) -> {
        return new TranslatableTextComponent("commands.earth.geotp.error", place, type, message);
    });

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("geotp")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(argument("coordinate", Vec2ArgumentType.create()).executes(GeoTeleportCommand::runCoordinate))
        );
        dispatcher.register(
                literal("geotp")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(argument("location", StringArgumentType.greedyString()).executes(GeoTeleportCommand::runLocation))
        );
    }

    private static int runCoordinate(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Vec2f location = Vec2ArgumentType.getVec2Argument(context, "coordinate");
        return run(context.getSource(), new CoordinateLocation(location.x, location.y));
    }

    private static int runLocation(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String location = StringArgumentType.getString(context, "location");
        return run(context.getSource(), new GeocodeLocation(location));
    }

    private static int run(ServerCommandSource source, CommandLocation location) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();

        EarthGeneratorConfig config = EarthGeneratorConfig.get(source.getWorld());
        if (config == null) {
            throw WRONG_WORLD.create();
        }

        Thread thread = new Thread(() -> {
            try {
                teleport(source, location.getCoordinate(player, config));
            } catch (CommandSyntaxException e) {
                source.sendError(TextFormatter.message(e.getRawMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();

        return 1;
    }

    private static void teleport(ServerCommandSource source, Coordinate coordinate) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();

        int blockX = MathHelper.floor(coordinate.getBlockX());
        int blockZ = MathHelper.floor(coordinate.getBlockZ());

        Chunk chunk = player.world.getChunk(blockX >> 4, blockZ >> 4);
        int height = chunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, blockX & 15, blockZ & 15);

        player.stopRiding();
        player.networkHandler.method_14363(coordinate.getBlockX(), height + 0.5, coordinate.getBlockZ(), 180.0F, 0.0F);
        player.velocityY = 0.0;
        player.onGround = true;

        source.sendFeedback(new TranslatableTextComponent("commands.earth.geotp.success", coordinate.getX(), coordinate.getZ()), true);
    }

    private interface CommandLocation {
        Coordinate getCoordinate(PlayerEntity player, EarthGeneratorConfig config) throws CommandSyntaxException;
    }

    private static class CoordinateLocation implements CommandLocation {
        private final double latitude;
        private final double longitude;

        private CoordinateLocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public Coordinate getCoordinate(PlayerEntity player, EarthGeneratorConfig config) {
            return new Coordinate(config.getGeoCoordinate(), this.latitude, this.longitude);
        }
    }

    private static class GeocodeLocation implements CommandLocation {
        private final String place;

        private GeocodeLocation(String place) {
            this.place = place;
        }

        @Override
        public Coordinate getCoordinate(PlayerEntity player, EarthGeneratorConfig config) throws CommandSyntaxException {
            try {
                Point2d coordinate = config.getGeocoder().get(this.place);
                if (coordinate == null) {
                    throw NOT_FOUND.create(this.place);
                }

                return new Coordinate(config.getGeoCoordinate(), coordinate.x, coordinate.y);
            } catch (IOException e) {
                Terrarium.LOGGER.error("Failed to get geocode for {}", this.place, e);
                throw GEOCODE_ERROR.create(this.place, e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
