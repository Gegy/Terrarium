package net.gegy1000.terrarium.client;

import net.fabricmc.fabric.events.client.ClientTickEvent;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.api.CustomLevelGenerator;
import net.gegy1000.terrarium.client.event.GuiChangeEvent;
import net.gegy1000.terrarium.client.gui.RemoteDataWarningGui;
import net.gegy1000.terrarium.server.event.WorldEvent;
import net.gegy1000.terrarium.server.message.TerrariumHandshakeMessage;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.World;

public class ClientEventHandler {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private static int gameTicks = 0;

    private static boolean awaitingLoad;
    private static boolean handshakeQueued;

    public static void register() {
        ClientTickEvent.CLIENT.register(ClientEventHandler::onTick);
        GuiChangeEvent.HANDLERS.register(ClientEventHandler::onGuiChange);

        WorldEvent.LOAD.register(ClientEventHandler::onJoinWorld);
    }

    private static void onJoinWorld(World world) {
        if (world.isClient) {
            CustomLevelGenerator customGenerator = CustomLevelGenerator.unwrap(world.getGeneratorType());
            if (customGenerator instanceof TerrariumGeneratorType && CLIENT.method_1496()) {
                awaitingLoad = true;
            }
            if (Terrarium.serverHasMod) {
                handshakeQueued = true;
            }
        }
    }

    private static void onTick(MinecraftClient client) {
        gameTicks++;

        if (client.player != null && client.player.age > 1) {
            if (awaitingLoad) {
                awaitingLoad = false;
                client.openGui(new RemoteDataWarningGui(client.currentGui));
            }
            if (handshakeQueued) {
                handshakeQueued = false;
                client.getNetworkHandler().sendPacket(TerrariumHandshakeMessage.createServerbound());
            }
        }
    }

    private static Gui onGuiChange(Gui newGui) {
        Gui currentGui = CLIENT.currentGui;
        if (currentGui instanceof RemoteDataWarningGui && !((RemoteDataWarningGui) currentGui).isComplete()) {
            ((RemoteDataWarningGui) currentGui).setParent(newGui);
            return currentGui;
        }
        return newGui;
    }

    public static int getGameTicks() {
        return ClientEventHandler.gameTicks;
    }
}
