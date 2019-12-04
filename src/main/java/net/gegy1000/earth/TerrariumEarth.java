package net.gegy1000.earth;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import net.gegy1000.earth.server.ServerProxy;
import net.gegy1000.earth.server.capability.HeightmapStore;
import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.command.GeoTeleportCommand;
import net.gegy1000.earth.server.command.GeoToolCommand;
import net.gegy1000.earth.server.config.TerrariumEarthConfig;
import net.gegy1000.earth.server.message.EarthMapGuiMessage;
import net.gegy1000.earth.server.message.EarthPanoramaMessage;
import net.gegy1000.earth.server.shared.ClimateRasterInitializer;
import net.gegy1000.earth.server.shared.RemoteIndexInitializer;
import net.gegy1000.earth.server.shared.SharedDataInitializers;
import net.gegy1000.earth.server.world.EarthWorldType;
import net.gegy1000.earth.server.world.data.EarthRemoteData;
import net.gegy1000.earth.server.world.data.GoogleGeocoder;
import net.gegy1000.earth.server.world.data.NominatimGeocoder;
import net.gegy1000.terrarium.server.capability.DelegatedStorage;
import net.gegy1000.terrarium.server.capability.VoidStorage;
import net.gegy1000.terrarium.server.world.data.source.Geocoder;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

@Mod.EventBusSubscriber
@Mod(modid = TerrariumEarth.MODID, name = "Terrarium: Earth", version = TerrariumEarth.VERSION, acceptedMinecraftVersions = "[1.12]", dependencies = "required-after:terrarium@[0.1.0,]")
public class TerrariumEarth {
    public static final String MODID = "earth";
    public static final String VERSION = "1.1.0";

    public static final String USER_AGENT = "terrarium-earth";

    public static final String CLIENT_PROXY = "net.gegy1000.earth.client.ClientProxy";
    public static final String SERVER_PROXY = "net.gegy1000.earth.server.ServerProxy";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    public static ServerProxy PROXY;

    public static final WorldType EARTH_TYPE = new EarthWorldType().create();

    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(TerrariumEarth.MODID);

    @CapabilityInject(EarthWorld.class)
    private static Capability<EarthWorld> worldCap;

    @CapabilityInject(HeightmapStore.class)
    private static Capability<HeightmapStore> heightmapCap;

    private static boolean deobfuscatedEnvironment;

    @Mod.EventHandler
    public static void onPreInit(FMLPreInitializationEvent event) {
        deobfuscatedEnvironment = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

        CapabilityManager.INSTANCE.register(EarthWorld.class, new VoidStorage<>(), EarthWorld.None::new);
        CapabilityManager.INSTANCE.register(HeightmapStore.class, new DelegatedStorage<>(), HeightmapStore::new);

        PROXY.onPreInit();

        SharedDataInitializers.add(
                new ClimateRasterInitializer(),
                new RemoteIndexInitializer()
        );

        Thread thread = new Thread(() -> {
            try {
                // TODO: SharedDataInitializer
                EarthRemoteData.load();
            } catch (IOException e) {
                LOGGER.warn("Failed to load remote Earth data {}", e.toString());
            } catch (Throwable t) {
                LOGGER.error("An unexpected exception occurred while loading remote data", t);
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

    @SubscribeEvent
    public static void onAttachChunkCapabilities(AttachCapabilitiesEvent<Chunk> event) {
        event.addCapability(new ResourceLocation(MODID, "heightmap"), new HeightmapStore());
    }

    public static Geocoder getPreferredGeocoder() {
        if (TerrariumEarthConfig.osmGeocoder || Strings.isNullOrEmpty(EarthRemoteData.info.getGeocoderKey())) {
            return new NominatimGeocoder();
        } else {
            return new GoogleGeocoder();
        }
    }

    public static boolean isDeobfuscatedEnvironment() {
        return deobfuscatedEnvironment;
    }

    public static Capability<EarthWorld> worldCap() {
        Preconditions.checkNotNull(worldCap, "earth world capability not yet initialized");
        return worldCap;
    }

    public static Capability<HeightmapStore> heightmapCap() {
        Preconditions.checkNotNull(heightmapCap, "earth column heightmap capability not yet initialized");
        return heightmapCap;
    }
}
