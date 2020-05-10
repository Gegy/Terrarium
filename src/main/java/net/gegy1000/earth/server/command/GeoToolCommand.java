package net.gegy1000.earth.server.command;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.message.EarthOpenMapMessage;
import net.gegy1000.earth.server.message.EarthPanoramaMessage;
import net.gegy1000.earth.server.world.data.DataPreloader;
import net.gegy1000.terrarium.server.TerrariumUserTracker;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.Optional;

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
        return DeferredTranslator.translateStringOrKey(sender, "commands.earth.geotool.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = CommandBase.getCommandSenderAsPlayer(sender);

        EarthWorld earth = player.world.getCapability(TerrariumEarth.worldCap(), null);
        if (earth != null) {
            ContainerUi.Builder builder = ContainerUi.builder(player)
                    .setTitle(DeferredTranslator.translate(player, new TextComponentTranslation("container.earth.geotool.name")));

            if (TerrariumUserTracker.usesTerrarium(player)) {
                String locate = DeferredTranslator.translateString(sender, "commands.earth.geotool.locate");
                builder.addElement(Items.COMPASS, TextFormatting.BOLD + locate, () -> this.openMap(player, earth, EarthOpenMapMessage.Type.LOCATE));

                String displayPanorama = DeferredTranslator.translateString(sender, "commands.earth.geotool.display_panorama");
                builder.addElement(Items.PAINTING, TextFormatting.BOLD + displayPanorama, () -> this.handlePanorama(player));

                if (DataPreloader.checkPermission(player)) {
                    String preloadWorld = DeferredTranslator.translateString(sender, "commands.earth.geotool.preload_world");
                    builder.addElement(Blocks.COMMAND_BLOCK, TextFormatting.BOLD + preloadWorld, () -> this.handlePreload(player, earth));
                }
            } else {
                String locate = DeferredTranslator.translateString(sender, "commands.earth.geotool.locate");
                builder.addElement(Items.COMPASS, TextFormatting.BOLD + locate, () -> this.handleLocate(player, earth));
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
        Coordinate coordinate = Coordinate.atBlock(player.posX, player.posZ).to(earth.getCrs());

        double longitude = coordinate.getX();
        double latitude = coordinate.getZ();

        String location = TextFormatting.BOLD.toString() + TextFormatting.UNDERLINE + String.format("%.5f, %.5f", latitude, longitude);
        player.sendMessage(DeferredTranslator.translate(player, new TextComponentTranslation("geotool.earth.locate.success", location)));
    }

    private void handlePreload(EntityPlayerMP player, EarthWorld earth) {
        Optional<DataPreloader> activeOpt = DataPreloader.active();
        if (activeOpt.isPresent()) {
            DataPreloader active = activeOpt.get();
            active.addWatcher(player);
        } else {
            this.openMap(player, earth, EarthOpenMapMessage.Type.PRELOAD);
        }
    }

    private void openMap(EntityPlayerMP player, EarthWorld earth, EarthOpenMapMessage.Type type) {
        Coordinate coordinate = Coordinate.atBlock(player.posX, player.posZ).to(earth.getCrs());

        double longitude = coordinate.getX();
        double latitude = coordinate.getZ();

        TerrariumEarth.NETWORK.sendTo(new EarthOpenMapMessage(latitude, longitude, type), player);
    }
}
