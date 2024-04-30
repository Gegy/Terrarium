package dev.gegy.terrarium.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.gegy.terrarium.backend.earth.EarthConfiguration;
import dev.gegy.terrarium.backend.earth.GeoCoords;
import dev.gegy.terrarium.backend.projection.Projection;
import dev.gegy.terrarium.world.generator.chunk.EarthChunkGenerator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class GeoTeleportCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("geotp")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS) && getEarthConfig(source.getServer()) != null)
                // The absolute poles extend to infinity in the Mercator projection, so restrict it
                .then(argument("latitude", doubleArg(-89.0, 89.0))
                        .then(argument("longitude", doubleArg(-180.0, 180.0))
                                .executes(context -> teleportByCoordinates(context, getDouble(context, "latitude"), getDouble(context, "longitude")))
                        )
                )
        );
    }

    private static int teleportByCoordinates(final CommandContext<CommandSourceStack> context, final double latitude, final double longitude) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();
        final ServerPlayer player = source.getPlayerOrException();
        teleportTo(player.getServer(), player, new GeoCoords(latitude, longitude));

        source.sendSuccess(() -> {
            final String formattedLatitude = String.format(Locale.ROOT, "%.3f", latitude);
            final String formattedLongitude = String.format(Locale.ROOT, "%.3f", longitude);
            return Component.translatable("commands.terrarium.geotp.coordinate.success", source.getDisplayName(), formattedLatitude, formattedLongitude);
        }, true);

        return 1;
    }

    private static void teleportTo(final MinecraftServer server, final ServerPlayer player, final GeoCoords coords) {
        final ServerLevel level = server.overworld();
        final EarthConfiguration earthConfig = getEarthConfigOrThrow(server);
        final Projection projection = earthConfig.projection();
        final ServerChunkCache chunkSource = level.getChunkSource();

        final double x = projection.blockX(coords);
        final double z = projection.blockZ(coords);
        final int y = chunkSource.getGenerator().getFirstFreeHeight(Mth.floor(x), Mth.floor(z), Heightmap.Types.MOTION_BLOCKING, level, chunkSource.randomState());
        player.teleportTo(level, x, y, z, player.getYRot(), player.getXRot());
    }

    private static EarthConfiguration getEarthConfigOrThrow(final MinecraftServer server) {
        return Objects.requireNonNull(getEarthConfig(server));
    }

    @Nullable
    private static EarthConfiguration getEarthConfig(final MinecraftServer server) {
        if (server.overworld().getChunkSource().getGenerator() instanceof final EarthChunkGenerator earthGenerator) {
            return earthGenerator.configuration();
        }
        return null;
    }
}
