package net.gegy1000.earth.server.command;

import com.google.common.base.Preconditions;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.command.debugger.DebugGeoProfile;
import net.gegy1000.earth.server.command.debugger.GeoDebugger;
import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.ecology.vegetation.Trees;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GeoDebugCommand extends CommandBase {
    private static final Path DEBUG = Paths.get("mods/terrarium/debug");
    private static final int RASTER_RADIUS = 128;
    private static final int RASTER_SIZE = RASTER_RADIUS * 2 + 1;

    @Override
    public String getName() {
        return "geodebug";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return DeferredTranslator.translateString(sender, "commands.earth.geodebug.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = CommandBase.getCommandSenderAsPlayer(sender);

        EarthWorld earth = player.world.getCapability(TerrariumEarth.worldCap(), null);
        if (earth != null) {
            GeoDebugger debug = GeoDebugger.from(player.world);

            ContainerUi.Builder builder = ContainerUi.builder(player)
                    .setTitle(DeferredTranslator.translate(player, new TextComponentTranslation("container.earth.geodebug.name")));

            builder.addElement(Items.FILLED_MAP, "Make Rasters", () -> this.openRasterUi(player, debug));

            builder.addElement(Items.WRITABLE_BOOK, "Take Profile Here", () -> {
                DebugGeoProfile profile = debug.takeProfile("Current Location", player.posX, player.posZ);
                profile.sendTo(player);
            });

            builder.addElement(Items.ENDER_PEARL, "Export Test Profiles", () -> {
                this.writeProfiles(debug.takeTestProfiles());
            });

            ContainerUi ui = builder.build();
            player.displayGUIChest(ui.createInventory());
        } else {
            throw DeferredTranslator.createException(player, "commands.earth.wrong_world");
        }
    }

    private void openRasterUi(EntityPlayerMP player, GeoDebugger debug) {
        ContainerUi.Builder builder = ContainerUi.builder(player)
                .setTitle(DeferredTranslator.translate(player, new TextComponentTranslation("container.earth.geodebug.name")));

        builder.addElement(Item.getItemFromBlock(Blocks.SAPLING), "Vegetation Suitability Index", () -> {
            this.writeRaster(player, debug.vegetation("acacia", Trees.ACACIA));
            this.writeRaster(player, debug.vegetation("birch", Trees.BIRCH));
            this.writeRaster(player, debug.vegetation("oak", Trees.OAK));
            this.writeRaster(player, debug.vegetation("jungle", Trees.JUNGLE));
            this.writeRaster(player, debug.vegetation("spruce", Trees.SPRUCE));
            this.writeRaster(player, debug.vegetation("pine", Trees.PINE));
        });

        builder.addElement(Items.CLAY_BALL, "Soil", () -> {
            this.writeRaster(player, debug.scaledHeatmap("clay_content", UByteRaster.sampler(EarthDataKeys.CLAY_CONTENT)));
            this.writeRaster(player, debug.scaledHeatmap("silt_content", UByteRaster.sampler(EarthDataKeys.SILT_CONTENT)));
            this.writeRaster(player, debug.scaledHeatmap("sand_content", UByteRaster.sampler(EarthDataKeys.SAND_CONTENT)));
        });

        builder.addElement(Items.FILLED_MAP, "Cover", () -> {
            EnumRaster.Sampler<Cover> sampler = EnumRaster.sampler(EarthDataKeys.COVER, Cover.NO);
            this.writeRaster(player, debug.cover("cover", sampler));
        });

        ContainerUi ui = builder.build();
        player.displayGUIChest(ui.createInventory());
    }

    private void writeRaster(EntityPlayerMP player, GeoDebugger.RasterSampler sampler) {
        TerrariumWorld terrarium = TerrariumWorld.get(player.world);
        Preconditions.checkNotNull(terrarium, "terrarium world was null");

        ColumnDataCache dataCache = terrarium.getDataCache();

        int x = MathHelper.floor(player.posX);
        int z = MathHelper.floor(player.posZ);

        DataView view = DataView.rect(x - RASTER_RADIUS, z - RASTER_RADIUS, RASTER_SIZE, RASTER_SIZE);

        BufferedImage image = sampler.sample(dataCache, view);

        try {
            if (!Files.exists(DEBUG)) Files.createDirectories(DEBUG);
            ImageIO.write(image, "png", DEBUG.resolve(sampler.name + ".png").toFile());
        } catch (IOException e) {
            TerrariumEarth.LOGGER.warn("Failed to write raster {}", sampler.name, e);
        }
    }

    private void writeProfiles(DebugGeoProfile[] profiles) {
        try {
            if (!Files.exists(DEBUG)) Files.createDirectories(DEBUG);

            Path path = DEBUG.resolve("debug_profiles.csv");

            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
                writer.println("Name,Latitude,Longitude," +
                        "Surface Elevation,Cover," +
                        "Mean Temperature,Min Temperature,Annual Rainfall," +
                        "Soil Class," +
                        "Silt Content,Sand Content,Clay Content," +
                        "Organic Carbon Content,Cation Exchange Capacity,Soil pH"
                );

                for (DebugGeoProfile profile : profiles) {
                    writer.print(profile.name + "," + profile.latitude + "," + profile.longitude + ",");
                    writer.print(profile.surfaceElevation + "," + profile.cover + ",");
                    writer.print(profile.meanTemperature + "," + profile.minTemperature + "," + profile.annualRainfall + ",");
                    writer.print(profile.soilClass + ",");
                    writer.print(profile.siltContent + "," + profile.sandContent + "," + profile.clayContent + ",");
                    writer.print(profile.organicCarbonContent + "," + profile.cationExchangeCapacity + "," + profile.soilPh + ",");
                    writer.println();
                }
            }
        } catch (IOException e) {
            TerrariumEarth.LOGGER.warn("Failed to write profiles", e);
        }
    }
}
