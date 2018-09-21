package net.gegy1000.earth;

import net.gegy1000.earth.server.EarthDecorationEventHandler;
import net.gegy1000.earth.server.ServerProxy;
import net.gegy1000.earth.server.capability.EarthCapability;
import net.gegy1000.earth.server.command.GeoTeleportCommand;
import net.gegy1000.earth.server.command.GeoToolCommand;
import net.gegy1000.earth.server.message.EarthMapGuiMessage;
import net.gegy1000.earth.server.message.EarthPanoramaMessage;
import net.gegy1000.earth.server.world.CoverDebugWorldType;
import net.gegy1000.earth.server.world.EarthWorldType;
import net.gegy1000.earth.server.world.pipeline.source.EarthRemoteData;
import net.gegy1000.earth.server.world.pipeline.source.SrtmHeightSource;
import net.gegy1000.terrarium.server.capability.VoidStorage;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
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

@Mod(modid = TerrariumEarth.MODID, name = "Terrarium: Earth", version = TerrariumEarth.VERSION, acceptedMinecraftVersions = "[1.12]", dependencies = "required-after:terrarium@[0.1.0,]")
public class TerrariumEarth {
    public static final String MODID = "earth";
    public static final String VERSION = "2.0.0-dev";

    public static final String CLIENT_PROXY = "net.gegy1000.earth.client.ClientProxy";
    public static final String SERVER_PROXY = "net.gegy1000.earth.server.ServerProxy";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    public static ServerProxy PROXY;

    public static final WorldType EARTH_TYPE = new EarthWorldType().create();
    public static final WorldType COVER_DEBUG_TYPE = new CoverDebugWorldType().create();

    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(TerrariumEarth.MODID);

    @CapabilityInject(EarthCapability.class)
    public static Capability<EarthCapability> earthCap;

    @Mod.EventHandler
    public static void onPreInit(FMLPreInitializationEvent event) {
        CapabilityManager.INSTANCE.register(EarthCapability.class, new VoidStorage<>(), EarthCapability.Impl.class);
        PROXY.onPreInit();

        MinecraftForge.TERRAIN_GEN_BUS.register(EarthDecorationEventHandler.class);
        MinecraftForge.ORE_GEN_BUS.register(EarthDecorationEventHandler.class);

        Thread thread = new Thread(() -> {
            EarthRemoteData.loadInfo();
            SrtmHeightSource.loadValidTiles();
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
}
