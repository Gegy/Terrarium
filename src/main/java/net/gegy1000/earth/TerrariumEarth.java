package net.gegy1000.earth;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import net.gegy1000.earth.server.ServerProxy;
import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.command.GeoDebugCommand;
import net.gegy1000.earth.server.command.GeoTeleportCommand;
import net.gegy1000.earth.server.command.GeoToolCommand;
import net.gegy1000.earth.server.config.TerrariumEarthConfig;
import net.gegy1000.earth.server.integration.bop.BoPIntegration;
import net.gegy1000.earth.server.integration.dyntrees.DynamicTreesIntegration;
import net.gegy1000.earth.server.message.DisplayDownloadMessage;
import net.gegy1000.earth.server.message.EarthOpenMapMessage;
import net.gegy1000.earth.server.message.EarthPanoramaMessage;
import net.gegy1000.earth.server.message.ModifyDownloadMessage;
import net.gegy1000.earth.server.message.OpenDownloadMessage;
import net.gegy1000.earth.server.message.UpdateDownloadMessage;
import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.earth.server.world.EarthWorldType;
import net.gegy1000.earth.server.world.compatibility.ModGeneratorInvoker;
import net.gegy1000.earth.server.world.compatibility.capability.CcColumnCompatibilityMetadata;
import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.gegy1000.earth.server.world.data.EarthApiKeys;
import net.gegy1000.earth.server.world.data.GoogleGeocoder;
import net.gegy1000.earth.server.world.data.NominatimGeocoder;
import net.gegy1000.terrarium.server.capability.DelegatingStorage;
import net.gegy1000.terrarium.server.capability.VoidStorage;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.data.source.Geocoder;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
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

import java.util.Map;

@Mod.EventBusSubscriber
@Mod(modid = TerrariumEarth.ID, name = "Terrarium: Earth", version = TerrariumEarth.VERSION, acceptedMinecraftVersions = "[1.12]", dependencies = "required-after:terrarium")
public class TerrariumEarth {
    public static final String ID = "earth";
    public static final String VERSION = "1.1.10";

    public static final String CLIENT_PROXY = "net.gegy1000.earth.client.ClientProxy";
    public static final String SERVER_PROXY = "net.gegy1000.earth.server.ServerProxy";

    public static final Logger LOGGER = LogManager.getLogger(ID);

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    public static ServerProxy PROXY;

    public static final TerrariumWorldType GENERIC_WORLD_TYPE = new EarthWorldType();
    public static final WorldType WORLD_TYPE = GENERIC_WORLD_TYPE.create();

    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(TerrariumEarth.ID);

    @CapabilityInject(EarthWorld.class)
    private static Capability<EarthWorld> worldCap;

    @CapabilityInject(CcColumnCompatibilityMetadata.class)
    private static Capability<CcColumnCompatibilityMetadata> ccCompatibilityGenerationCap;

    private static boolean deobfuscatedEnvironment;

    public static boolean hasBiomesOPlenty;

    @Mod.EventHandler
    public static void onPreInit(FMLPreInitializationEvent event) {
        deobfuscatedEnvironment = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

        CapabilityManager.INSTANCE.register(EarthWorld.class, new VoidStorage<>(), EarthWorld.None::new);
        CapabilityManager.INSTANCE.register(CcColumnCompatibilityMetadata.class, new DelegatingStorage<>(NBTTagByte.class), CcColumnCompatibilityMetadata::new);

        NETWORK.registerMessage(EarthOpenMapMessage.Handler.class, EarthOpenMapMessage.class, 0, Side.CLIENT);
        NETWORK.registerMessage(EarthPanoramaMessage.Handler.class, EarthPanoramaMessage.class, 1, Side.CLIENT);

        NETWORK.registerMessage(OpenDownloadMessage.Handler.class, OpenDownloadMessage.class, 2, Side.SERVER);
        NETWORK.registerMessage(ModifyDownloadMessage.Handler.class, ModifyDownloadMessage.class, 3, Side.SERVER);
        NETWORK.registerMessage(DisplayDownloadMessage.Handler.class, DisplayDownloadMessage.class, 4, Side.CLIENT);
        NETWORK.registerMessage(UpdateDownloadMessage.Handler.class, UpdateDownloadMessage.class, 5, Side.CLIENT);

        if (Loader.isModLoaded("biomesoplenty")) {
            hasBiomesOPlenty = true;
            BoPIntegration.setup();
        }

        if (Loader.isModLoaded("dynamictrees")) {
            DynamicTreesIntegration.setup();
        }
    }

    @Mod.EventHandler
    public static void onInit(FMLInitializationEvent event) {
        CoverMarkers.register();
    }

    @Mod.EventHandler
    public static void onPostInit(FMLPostInitializationEvent event) {
        PROXY.onPostInit();

        ModGeneratorInvoker.collectGeneratorExclusions();
    }

    @NetworkCheckHandler
    public static boolean onCheckNetwork(Map<String, String> mods, Side side) {
        return !mods.containsKey(TerrariumEarth.ID) || mods.get(TerrariumEarth.ID).equals(VERSION);
    }

    @Mod.EventHandler
    public static void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new GeoTeleportCommand());
        event.registerServerCommand(new GeoToolCommand());

        if (deobfuscatedEnvironment) {
            event.registerServerCommand(new GeoDebugCommand());
        }
    }

    public static Geocoder getPreferredGeocoder() {
        EarthApiKeys keys = SharedEarthData.instance().get(SharedEarthData.API_KEYS);
        if (TerrariumEarthConfig.osmGeocoder || (keys == null || Strings.isNullOrEmpty(keys.getGeocoderKey()))) {
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

    public static Capability<CcColumnCompatibilityMetadata> ccCompatibilityGenerationCap() {
        Preconditions.checkNotNull(ccCompatibilityGenerationCap, "cubic chunks compatibility generation capability not yet initialized");
        return ccCompatibilityGenerationCap;
    }
}
