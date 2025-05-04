package dev.gegy.terrarium.integration.distant_horizons;

import com.mojang.logging.LogUtils;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.interfaces.world.IDhApiLevelWrapper;
import com.seibel.distanthorizons.api.methods.events.DhApiEventRegister;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiLevelLoadEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiEventParam;
import com.seibel.distanthorizons.api.objects.DhApiResult;
import dev.gegy.terrarium.world.GeoProvider;
import dev.gegy.terrarium.world.GeoProviderHolder;
import dev.gegy.terrarium.world.generator.chunk.GeoChunkGenerator;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;

public class DistantHorizonsIntegration {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean checkApiVersion() {
        final int apiMajor = DhApi.getApiMajorVersion();
        final int apiMinor = DhApi.getApiMinorVersion();
        final int apiPatch = DhApi.getApiPatchVersion();
        if (apiMajor < 4) {
            LOGGER.warn("Detected Distant Horizons {}, but API {}.{}.{} is too old - won't enable integration with Terrarium", DhApi.getModVersion(), apiMajor, apiMinor, apiPatch);
            return false;
        }
        LOGGER.info("Detected Distant Horizons {} (API {}.{}.{}), enabling integration with Terrarium", DhApi.getModVersion(), apiMajor, apiMinor, apiPatch);
        return true;
    }

    public static void bootstrap() {
        if (!checkApiVersion()) {
            return;
        }
        DhApiEventRegister.on(DhApiLevelLoadEvent.class, new DhApiLevelLoadEvent() {
            @Override
            public void onLevelLoad(final DhApiEventParam<EventParam> param) {
                DistantHorizonsIntegration.onLevelLoad(param.value.levelWrapper);
            }
        });
    }

    private static void onLevelLoad(final IDhApiLevelWrapper levelWrapper) {
        if (levelWrapper.getWrappedMcObject() instanceof final ServerLevel level
                && level.getChunkSource().getGenerator() instanceof final GeoChunkGenerator geoGenerator
        ) {
            final GeoProvider geoProvider = GeoProviderHolder.get(level);
            if (geoProvider == null) {
                return;
            }
            final GeoLodGenerator lodGenerator = new GeoLodGenerator(levelWrapper, geoProvider, geoGenerator);
            final DhApiResult<Void> result = DhApi.worldGenOverrides.registerWorldGeneratorOverride(levelWrapper, lodGenerator);
            if (!result.success) {
                LOGGER.warn("Failed to register Terrarium LoD generator: {}", result.message);
            }
        }
    }
}
