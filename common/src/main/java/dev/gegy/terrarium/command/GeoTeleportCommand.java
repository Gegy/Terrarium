package dev.gegy.terrarium.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.gegy.terrarium.Terrarium;
import dev.gegy.terrarium.backend.earth.EarthConfiguration;
import dev.gegy.terrarium.backend.earth.GeoCoords;
import dev.gegy.terrarium.backend.earth.geocoder.Geocoder;
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

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class GeoTeleportCommand {
    private static final int MIN_SUGGESTION_QUERY_CHARACTERS = 2;
    private static final int MAX_SUGGESTION_COUNT = 3;

    private static final Duration SUGGESTION_DELAY = Duration.ofMillis(300);

    private static final Component RATE_LIMITED_MESSAGE = Component.translatable("commands.terrarium.geotp.lookup.rate_limited");
    private static final Component UNKNOWN_ERROR_MESSAGE = Component.translatable("commands.terrarium.geotp.lookup.unknown_error");

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        final CommandSuggestionLimiter suggestionLimiter = new CommandSuggestionLimiter(SUGGESTION_DELAY);
        dispatcher.register(literal("geotp")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS) && getEarthConfig(source.getServer()) != null)
                // The absolute poles extend to infinity in the Mercator projection, so restrict it
                .then(argument("latitude", doubleArg(-89.0, 89.0))
                        .then(argument("longitude", doubleArg(-180.0, 180.0))
                                .executes(context -> teleportByCoordinates(context, getDouble(context, "latitude"), getDouble(context, "longitude")))
                        )
                )
                .then(argument("query", greedyString())
                        .suggests((context, builder) -> listSuggestions(context, builder, suggestionLimiter))
                        .executes(context -> teleportByQuery(context, getString(context, "query")))
                )
        );
    }

    private static CompletableFuture<Suggestions> listSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder, final CommandSuggestionLimiter suggestionLimiter) throws CommandSyntaxException {
        final String query = builder.getRemaining().trim();
        if (query.length() < MIN_SUGGESTION_QUERY_CHARACTERS) {
            return builder.buildFuture();
        }
        final UUID playerId = context.getSource().getPlayerOrException().getUUID();
        return suggestionLimiter.submit(playerId, () -> Terrarium.geocoder().listSuggestions(query)).thenApply(suggestions -> {
            if (suggestions.isEmpty()) {
                return builder.build();
            }
            int i = 0;
            for (final String suggestion : suggestions.get()) {
                builder.suggest(suggestion);
                if (++i >= MAX_SUGGESTION_COUNT) {
                    break;
                }
            }
            return builder.build();
        });
    }

    private static int teleportByQuery(final CommandContext<CommandSourceStack> context, final String query) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();
        final ServerPlayer player = source.getPlayerOrException();
        Terrarium.geocoder().lookup(query).whenCompleteAsync((coordinates, throwable) -> {
            if (throwable != null) {
                handleQueryError(throwable, source);
                return;
            }
            if (coordinates.isPresent()) {
                teleportTo(source, player, coordinates.get());
            } else {
                source.sendFailure(Component.translatable("commands.terrarium.geotp.lookup.not_found", query));
            }
        }, source.getServer());
        return 1;
    }

    private static void handleQueryError(final Throwable throwable, final CommandSourceStack source) {
        if (throwable instanceof final Geocoder.LookupException lookupException) {
            switch (lookupException.type()) {
                case RATE_LIMITED -> source.sendFailure(RATE_LIMITED_MESSAGE);
                case UNKNOWN_ERROR -> source.sendFailure(UNKNOWN_ERROR_MESSAGE);
            }
        } else {
            source.sendFailure(UNKNOWN_ERROR_MESSAGE);
        }
    }

    private static int teleportByCoordinates(final CommandContext<CommandSourceStack> context, final double latitude, final double longitude) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();
        teleportTo(source, source.getPlayerOrException(), new GeoCoords(latitude, longitude));

        return 1;
    }

    private static void teleportTo(final CommandSourceStack source, final ServerPlayer player, final GeoCoords coords) {
        final ServerLevel level = source.getServer().overworld();
        final EarthConfiguration earthConfig = getEarthConfigOrThrow(source.getServer());
        final Projection projection = earthConfig.projection();
        final ServerChunkCache chunkSource = level.getChunkSource();

        final double x = projection.blockX(coords);
        final double z = projection.blockZ(coords);
        final int y = chunkSource.getGenerator().getFirstFreeHeight(Mth.floor(x), Mth.floor(z), Heightmap.Types.MOTION_BLOCKING, level, chunkSource.randomState());
        player.teleportTo(level, x, y, z, Set.of(), player.getYRot(), player.getXRot(), true);

        source.sendSuccess(() -> {
            final String formattedLatitude = String.format(Locale.ROOT, "%.3f", coords.lat());
            final String formattedLongitude = String.format(Locale.ROOT, "%.3f", coords.lon());
            return Component.translatable("commands.terrarium.geotp.coordinate.success", source.getDisplayName(), formattedLatitude, formattedLongitude);
        }, true);
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
