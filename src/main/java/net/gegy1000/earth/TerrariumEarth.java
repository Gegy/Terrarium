package net.gegy1000.earth;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import net.gegy1000.earth.server.ServerProxy;
import net.gegy1000.earth.server.capability.EarthCapability;
import net.gegy1000.earth.server.command.GeoTeleportCommand;
import net.gegy1000.earth.server.command.GeoToolCommand;
import net.gegy1000.earth.server.config.TerrariumEarthConfig;
import net.gegy1000.earth.server.message.EarthMapGuiMessage;
import net.gegy1000.earth.server.message.EarthPanoramaMessage;
import net.gegy1000.earth.server.world.CoverDebugWorldType;
import net.gegy1000.earth.server.world.EarthWorldType;
import net.gegy1000.earth.server.world.pipeline.source.EarthRemoteData;
import net.gegy1000.earth.server.world.pipeline.source.GoogleGeocoder;
import net.gegy1000.earth.server.world.pipeline.source.NominatimGeocoder;
import net.gegy1000.earth.server.world.pipeline.source.SrtmHeightSource;
import net.gegy1000.earth.server.world.pipeline.source.WorldClimateDataset;
import net.gegy1000.terrarium.server.capability.VoidStorage;
import net.gegy1000.terrarium.server.world.pipeline.source.Geocoder;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Mod(modid = TerrariumEarth.MODID, name = "Terrarium: Earth", version = TerrariumEarth.VERSION, acceptedMinecraftVersions = "[1.12]", dependencies = "required-after:terrarium@[0.1.0,]")
public class TerrariumEarth {
    public static final String MODID = "earth";
    public static final String VERSION = "1.1.0";

    public static final String CLIENT_PROXY = "net.gegy1000.earth.client.ClientProxy";
    public static final String SERVER_PROXY = "net.gegy1000.earth.server.ServerProxy";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    public static ServerProxy PROXY;

    public static final WorldType EARTH_TYPE = new EarthWorldType().create();
    public static final WorldType COVER_DEBUG_TYPE = new CoverDebugWorldType().create();

    private static WorldClimateDataset climateDataset;

    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(TerrariumEarth.MODID);

    @CapabilityInject(EarthCapability.class)
    public static Capability<EarthCapability> earthCap;

    @Mod.EventHandler
    public static void onPreInit(FMLPreInitializationEvent event) {
        CapabilityManager.INSTANCE.register(EarthCapability.class, new VoidStorage<>(), EarthCapability.None::new);
        PROXY.onPreInit();

        Thread thread = new Thread(() -> {
            try {
                EarthRemoteData.loadInfo();
                SrtmHeightSource.loadValidTiles();
            } catch (Throwable t) {
                LOGGER.warn("Failed to load remote Earth data {}", t.toString());
            }
        }, "Terrarium Remote Load");
        thread.setDaemon(true);
        thread.start();

        NETWORK.registerMessage(EarthMapGuiMessage.Handler.class, EarthMapGuiMessage.class, 0, Side.CLIENT);
        NETWORK.registerMessage(EarthPanoramaMessage.Handler.class, EarthPanoramaMessage.class, 1, Side.CLIENT);
    }

    @Mod.EventHandler
    public static void onInit(FMLInitializationEvent event) {
        PROXY.onInit();
    }

    @Mod.EventHandler
    public static void onPostInit(FMLPostInitializationEvent event) {
        PROXY.onPostInit();

        ProgressManager.ProgressBar bar = ProgressManager.push("Loading Basic Datasets", 1);
        bar.step("Global Climate");

        try {
            InputStream input = WorldClimateDataset.class.getResourceAsStream("/data/earth/global_climate.dat");
            climateDataset = WorldClimateDataset.parse(input);
        } catch (IOException e) {
            LOGGER.error("Failed to load world climate dataset!", e);
        }

        ProgressManager.pop(bar);
    }

    @NetworkCheckHandler
    public static boolean onCheckNetwork(Map<String, String> mods, Side side) {
        return !mods.containsKey(TerrariumEarth.MODID) || mods.get(TerrariumEarth.MODID).equals(VERSION);
    }

    @Mod.EventHandler
    public static void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new GeoTeleportCommand());
        event.registerServerCommand(new GeoToolCommand());
    }

    @Nonnull
    public static WorldClimateDataset getClimateDataset() {
        Preconditions.checkNotNull(climateDataset, "Climate dataset not yet loaded!");
        return climateDataset;
    }

    public static Geocoder getPreferredGeocoder() {
        if (TerrariumEarthConfig.osmGeocoder || Strings.isNullOrEmpty(EarthRemoteData.info.getGeocoderKey())) {
            return new NominatimGeocoder();
        } else {
            return new GoogleGeocoder();
        }
    }
}
