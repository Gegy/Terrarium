package net.gegy1000.earth.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.gegy1000.earth.server.message.EarthMapGuiMessage;
import net.gegy1000.earth.server.message.EarthPanoramaMessage;
import net.gegy1000.earth.server.world.EarthGeneratorConfig;
import net.gegy1000.terrarium.server.TerrariumHandshakeTracker;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TextFormat;
import net.minecraft.text.TranslatableTextComponent;

import static net.minecraft.server.command.ServerCommandManager.literal;

public class GeoToolCommand {
    private static final SimpleCommandExceptionType WRONG_WORLD = new SimpleCommandExceptionType(
            new TranslatableTextComponent("commands.earth.wrong_world")
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("geotool").executes(GeoToolCommand::run)
        );
    }

    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        EarthGeneratorConfig config = EarthGeneratorConfig.get(player.world);
        if (config != null) {
            ContainerUi.Builder builder = ContainerUi.builder(player)
                    .withTitle(new TranslatableTextComponent("container.earth.geotool.name"))
                    .withElement(Items.COMPASS, TextFormat.BOLD + "Where am I?", () -> handleLocate(player, config));

            if (TerrariumHandshakeTracker.isFriendly(player)) {
                builder = builder
                        .withElement(Items.ENDER_PEARL, TextFormat.BOLD + "Go to place", () -> handleTeleport(player, config))
                        .withElement(Items.PAINTING, TextFormat.BOLD + "Display Panorama", () -> handlePanorama(player));
            }

            ContainerUi ui = builder.build();
            player.openInventory(ui.createInventory());
        } else {
            throw WRONG_WORLD.create();
        }

        return 1;
    }

    private static void handleLocate(ServerPlayerEntity player, EarthGeneratorConfig earthData) {
        double latitude = earthData.getLatitude(player.x, player.z);
        double longitude = earthData.getLongitude(player.x, player.z);
        if (TerrariumHandshakeTracker.isFriendly(player)) {
            player.networkHandler.sendPacket(EarthMapGuiMessage.create(latitude, longitude, EarthMapGuiMessage.Type.LOCATE));
        } else {
            String location = TextFormat.BOLD.toString() + TextFormat.UNDERLINE + String.format("%.5f, %.5f", latitude, longitude);
            player.addChatMessage(new TranslatableTextComponent("geotool.earth.locate.success", location), false);
        }
    }

    private static void handleTeleport(ServerPlayerEntity player, EarthGeneratorConfig earthData) {
        double latitude = earthData.getLatitude(player.x, player.z);
        double longitude = earthData.getLongitude(player.x, player.z);
        player.networkHandler.sendPacket(EarthMapGuiMessage.create(latitude, longitude, EarthMapGuiMessage.Type.TELEPORT));
    }

    private static void handlePanorama(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(EarthPanoramaMessage.create());
    }
}
