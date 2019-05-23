package net.gegy1000.earth.server.command;

import com.google.common.base.Preconditions;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.capability.EarthCapability;
import net.gegy1000.earth.server.message.EarthMapGuiMessage;
import net.gegy1000.earth.server.message.EarthPanoramaMessage;
import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.terrarium.server.TerrariumHandshakeTracker;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataEntry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.concurrent.CompletableFuture;

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

        EarthCapability earthData = player.world.getCapability(TerrariumEarth.earthCap, null);
        if (earthData != null) {
            ContainerUi.Builder builder = ContainerUi.builder(player)
                    .withTitle(DeferredTranslator.translate(player, new TextComponentTranslation("container.earth.geotool.name")))
                    .withElement(Items.COMPASS, TextFormatting.BOLD + "Where am I?", () -> this.handleLocate(player, earthData));

            if (TerrariumHandshakeTracker.isFriendly(player)) {
                builder = builder
                        .withElement(Items.ENDER_PEARL, TextFormatting.BOLD + "Go to place", () -> this.handleTeleport(player, earthData))
                        .withElement(Items.PAINTING, TextFormatting.BOLD + "Display Panorama", () -> this.handlePanorama(player));
            }

            if (TerrariumEarth.isDeobfuscatedEnvironment()) {
                builder = builder.withElement(Items.REDSTONE, TextFormatting.BOLD + "Debug Info", () -> this.handleDebug(player, earthData));
            }

            ContainerUi ui = builder.build();
            player.displayGUIChest(ui.createInventory());
        } else {
            throw DeferredTranslator.createException(player, "commands.earth.wrong_world");
        }
    }

    private void handleLocate(EntityPlayerMP player, EarthCapability earthData) {
        double latitude = earthData.getLatitude(player.posX, player.posZ);
        double longitude = earthData.getLongitude(player.posX, player.posZ);
        if (TerrariumHandshakeTracker.isFriendly(player)) {
            TerrariumEarth.NETWORK.sendTo(new EarthMapGuiMessage(latitude, longitude, EarthMapGuiMessage.Type.LOCATE), player);
        } else {
            String location = TextFormatting.BOLD.toString() + TextFormatting.UNDERLINE + String.format("%.5f, %.5f", latitude, longitude);
            player.sendMessage(DeferredTranslator.translate(player, new TextComponentTranslation("geotool.earth.locate.success", location)));
        }
    }

    private void handleTeleport(EntityPlayerMP player, EarthCapability earthData) {
        double latitude = earthData.getLatitude(player.posX, player.posZ);
        double longitude = earthData.getLongitude(player.posX, player.posZ);
        TerrariumEarth.NETWORK.sendTo(new EarthMapGuiMessage(latitude, longitude, EarthMapGuiMessage.Type.TELEPORT), player);
    }

    private void handlePanorama(EntityPlayerMP player) {
        TerrariumEarth.NETWORK.sendTo(new EarthPanoramaMessage(), player);
    }

    private void handleDebug(EntityPlayerMP player, EarthCapability earthData) {
        double latitude = earthData.getLatitude(player.posX, player.posZ);
        double longitude = earthData.getLongitude(player.posX, player.posZ);
        int blockX = MathHelper.floor(player.posX);
        int blockZ = MathHelper.floor(player.posZ);

        TerrariumWorldData worldData = TerrariumWorldData.get(player.world);
        Preconditions.checkNotNull(worldData, "terrarium world data was null");

        ChunkPos columnPos = new ChunkPos(blockX >> 4, blockZ >> 4);

        ColumnDataCache dataCache = worldData.getDataCache();

        ColumnDataEntry.Handle handle = dataCache.acquireEntry(columnPos);
        CompletableFuture<ColumnData> future = handle.future();

        future.whenComplete((columnData, throwable) -> {
            handle.release();

            if (throwable != null) {
                TerrariumEarth.LOGGER.error("Failed to load debug info", throwable);
                return;
            }

            int localX = blockX - columnPos.getXStart();
            int localZ = blockZ - columnPos.getZStart();

            player.sendMessage(new TextComponentString(TextFormatting.BOLD + String.format("Debug Info at %.4f, %.4f", latitude, longitude)));

            columnData.get(EarthDataKeys.AVERAGE_TEMPERATURE).ifPresent(rainfallRaster -> {
                float temperature = rainfallRaster.get(localX, localZ);
                player.sendMessage(new TextComponentString(TextFormatting.AQUA + String.format("Mean Temperature: %s%.2f°C", TextFormatting.RESET, temperature)));
            });

            columnData.get(EarthDataKeys.ANNUAL_RAINFALL).ifPresent(rainfallRaster -> {
                short rainfall = rainfallRaster.get(localX, localZ);
                player.sendMessage(new TextComponentString(TextFormatting.AQUA + String.format("Yearly Rainfall: %s%smm", TextFormatting.RESET, rainfall)));
            });
        });
    }
}
