package net.gegy1000.terrarium

import net.gegy1000.terrarium.server.ServerProxy
import net.gegy1000.terrarium.server.command.GeoTeleportCommand
import net.gegy1000.terrarium.server.world.EarthWorldType
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import org.apache.logging.log4j.LogManager

@Mod(
        modid = Terrarium.MODID,
        name = Terrarium.NAME,
        version = Terrarium.VERSION,
        dependencies = Terrarium.DEPENDENCIES,
        modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter"
)
object Terrarium {
    const val MODID = "terrarium"
    const val NAME = "Terrarium"
    const val VERSION = "1.0.0"
    const val DEPENDENCIES = "required-after:forgelin@[1.4.2,)"

    const val CLIENT_PROXY = "net.gegy1000.terrarium.client.ClientProxy"
    const val SERVER_PROXY = "net.gegy1000.terrarium.server.ServerProxy"

    val LOGGER = LogManager.getLogger(MODID)!!

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    lateinit var PROXY: ServerProxy

    @Mod.EventHandler
    fun onPreInit(event: FMLPreInitializationEvent) {
        EarthWorldType.INSTANCE
        PROXY.onPreInit()
    }

    @Mod.EventHandler
    fun onInit(event: FMLInitializationEvent) {
        PROXY.onInit()
    }

    @Mod.EventHandler
    fun onPostInit(event: FMLPostInitializationEvent) {
        PROXY.onPostInit()
    }

    @Mod.EventHandler
    fun onServerStarting(event: FMLServerStartingEvent) {
        event.registerServerCommand(GeoTeleportCommand())
    }
}