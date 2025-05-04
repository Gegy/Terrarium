package dev.gegy.terrarium;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import dev.gegy.terrarium.backend.earth.ApiKeys;
import dev.gegy.terrarium.backend.earth.EarthTiles;
import dev.gegy.terrarium.backend.earth.GeoParameters;
import dev.gegy.terrarium.backend.earth.geocoder.Geocoder;
import dev.gegy.terrarium.backend.earth.geocoder.GoogleGeocoder;
import dev.gegy.terrarium.backend.expr.classifier.ClassifierNode;
import dev.gegy.terrarium.backend.expr.predictor.Predictor;
import dev.gegy.terrarium.backend.expr.predictor.PredictorNode;
import dev.gegy.terrarium.backend.loader.ConcurrencyLimiter;
import dev.gegy.terrarium.backend.tile.TileCache;
import dev.gegy.terrarium.command.GeoTeleportCommand;
import dev.gegy.terrarium.integration.distant_horizons.DistantHorizonsIntegration;
import dev.gegy.terrarium.registry.HolderClassifierNode;
import dev.gegy.terrarium.registry.HolderPredictorNode;
import dev.gegy.terrarium.registry.TerrariumRegistries;
import dev.gegy.terrarium.world.generator.biome.TerrariumBiomeSources;
import dev.gegy.terrarium.world.generator.chunk.TerrariumChunkGenerators;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nullable;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class Terrarium {
    public static final String ID = "terrarium";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .executor(Util.ioPool())
            .build();

    @Nullable
    private static EarthTiles.Config tiles;
    @Nullable
    private static Codec<PredictorNode<GeoParameters>> predictorCodec;
    @Nullable
    private static Codec<ClassifierNode<GeoParameters, Holder<Biome>>> biomeClassifierCodec;
    @Nullable
    private static CompletableFuture<Geocoder> geocoder;

    private static boolean developmentEnvironment;

    public static void bootstrap(final Path terrariumDirectory, final Registry<Predictor<GeoParameters>> builtinPredictors, final boolean developmentEnvironment, final PlatformBootstrap platform) {
        Terrarium.developmentEnvironment = developmentEnvironment;

        tiles = new EarthTiles.Config(
                HTTP_CLIENT,
                new ConcurrencyLimiter(16),
                terrariumDirectory.resolve("cache2"),
                Util.backgroundExecutor(),
                Util.ioPool()
        );

        geocoder = ApiKeys.fetch(HTTP_CLIENT).thenApply(apiKeys ->
                new GoogleGeocoder(HTTP_CLIENT, apiKeys)
                        .limitConcurrency(new ConcurrencyLimiter(2))
                        .cached()
        );

        GeoParameters.forEachFeature((id, predictor) ->
                Registry.register(builtinPredictors, ResourceLocation.fromNamespaceAndPath(Terrarium.ID, id), predictor)
        );

        final PredictorNode.Codecs<GeoParameters> predictorCodecs = PredictorNode.createCodecs(
                builtinPredictors.byNameCodec(),
                directCodec -> RegistryFileCodec.create(TerrariumRegistries.PREDICTOR, directCodec).xmap(HolderPredictorNode::new, node -> {
                    if (node instanceof final HolderPredictorNode<GeoParameters> holder) {
                        return holder.holder();
                    }
                    // Parsing will never construct this case, but we should be able to encode a node tree without Holder-wrapping
                    return Holder.direct(node);
                })
        );
        predictorCodec = predictorCodecs.externalCodec();

        biomeClassifierCodec = ClassifierNode.createCodec(
                predictorCodec,
                Biome.CODEC,
                directCodec -> RegistryFileCodec.create(TerrariumRegistries.BIOME_CLASSIFIER, directCodec).xmap(HolderClassifierNode::new, node -> {
                    if (node instanceof final HolderClassifierNode<GeoParameters, Holder<Biome>> holder) {
                        return holder.holder();
                    }
                    // Parsing will never construct this case, but we should be able to encode a node tree without Holder-wrapping
                    return Holder.direct(node);
                })
        );

        platform.initializeRegistries(predictorCodecs.directCodec(), biomeClassifierCodec);

        TerrariumChunkGenerators.bootstrap();
        TerrariumBiomeSources.bootstrap();

        if (platform.isModLoaded("distanthorizons")) {
            DistantHorizonsIntegration.bootstrap();
        }
    }

    public static void registerCommands(final CommandDispatcher<CommandSourceStack> dispatcher, final CommandBuildContext context) {
        GeoTeleportCommand.register(dispatcher);
    }

    public static EarthTiles createTiles(final TileCache cache) {
        final EarthTiles.Config tiles = Objects.requireNonNull(Terrarium.tiles, "Terrarium was not bootstrapped");
        return tiles.create(cache);
    }

    public static Codec<PredictorNode<GeoParameters>> predictorCodec() {
        return Objects.requireNonNull(predictorCodec, "Terrarium was not bootstrapped");
    }

    public static Codec<ClassifierNode<GeoParameters, Holder<Biome>>> biomeClassifierCodec() {
        return Objects.requireNonNull(biomeClassifierCodec, "Terrarium was not bootstrapped");
    }

    public static Geocoder geocoder() {
        return Objects.requireNonNull(geocoder, "Terrarium was not bootstrapped").join();
    }

    public static boolean isDevelopmentEnvironment() {
        return developmentEnvironment;
    }

    public interface PlatformBootstrap {
        void initializeRegistries(Codec<PredictorNode<GeoParameters>> predictorCodec, Codec<ClassifierNode<GeoParameters, Holder<Biome>>> biomeClassifierCodec);

        boolean isModLoaded(String id);
    }
}
