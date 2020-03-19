package net.gegy1000.earth.server.command;

import com.mojang.authlib.GameProfile;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.message.EarthOpenMapMessage;
import net.gegy1000.earth.server.message.EarthPanoramaMessage;
import net.gegy1000.terrarium.server.TerrariumUserTracker;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class GeoToolCommand extends CommandBase {
    @Override
    public String getName() {
        return "geotool";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return DeferredTranslator.translateString(sender, "commands.earth.geotool.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = CommandBase.getCommandSenderAsPlayer(sender);

        EarthWorld earth = player.world.getCapability(TerrariumEarth.worldCap(), null);
        if (earth != null) {
            ContainerUi.Builder builder = ContainerUi.builder(player)
                    .setTitle(DeferredTranslator.translate(player, new TextComponentTranslation("container.earth.geotool.name")))
                    .addElement(Items.COMPASS, TextFormatting.BOLD + "Where am I?", () -> this.handleLocate(player, earth));

            if (TerrariumUserTracker.usesTerrarium(player)) {
                builder.addElement(Items.ENDER_PEARL, TextFormatting.BOLD + "Go to place", () -> this.handleTeleport(player, earth));
                builder.addElement(Items.PAINTING, TextFormatting.BOLD + "Display Panorama", () -> this.handlePanorama(player));

                if (hasPermission(player, 4)) {
                    builder.addElement(Blocks.COMMAND_BLOCK, TextFormatting.BOLD + "Preload World", () -> this.handlePreload(player, earth));
                }
            }

            ContainerUi ui = builder.build();
            player.displayGUIChest(ui.createInventory());
        } else {
            throw DeferredTranslator.createException(player, "commands.earth.wrong_world");
        }
    }

    private void handlePanorama(EntityPlayerMP player) {
        TerrariumEarth.NETWORK.sendTo(new EarthPanoramaMessage(), player);
    }

    private void handleLocate(EntityPlayerMP player, EarthWorld earth) {
        if (TerrariumUserTracker.usesTerrarium(player)) {
            this.openMap(player, earth, EarthOpenMapMessage.Type.LOCATE);
            return;
        }

        Coordinate coordinate = Coordinate.atBlock(player.posX, player.posZ).to(earth.getCrs());

        double longitude = coordinate.getX();
        double latitude = coordinate.getZ();

        String location = TextFormatting.BOLD.toString() + TextFormatting.UNDERLINE + String.format("%.5f, %.5f", latitude, longitude);
        player.sendMessage(DeferredTranslator.translate(player, new TextComponentTranslation("geotool.earth.locate.success", location)));
    }

    private void handleTeleport(EntityPlayerMP player, EarthWorld earth) {
        this.openMap(player, earth, EarthOpenMapMessage.Type.TELEPORT);
    }

    private void handlePreload(EntityPlayerMP player, EarthWorld earth) {
        this.openMap(player, earth, EarthOpenMapMessage.Type.PRELOAD);
    }

    private void openMap(EntityPlayerMP player, EarthWorld earth, EarthOpenMapMessage.Type type) {
        Coordinate coordinate = Coordinate.atBlock(player.posX, player.posZ).to(earth.getCrs());

        double longitude = coordinate.getX();
        double latitude = coordinate.getZ();

        TerrariumEarth.NETWORK.sendTo(new EarthOpenMapMessage(latitude, longitude, type), player);
    }

    private static boolean hasPermission(EntityPlayerMP player, int level) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;

        PlayerList players = server.getPlayerList();
        GameProfile profile = player.getGameProfile();
        if (!players.canSendCommands(profile)) {
            return false;
        }

        UserListOpsEntry op = players.getOppedPlayers().getEntry(profile);
        if (op != null) {
            return op.getPermissionLevel() >= level;
        } else {
            return server.getOpPermissionLevel() >= level;
        }
    }
}
