package net.gegy1000.earth.server.command;

import com.google.common.base.Preconditions;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.vegetation.Trees;
import net.gegy1000.earth.server.world.ecology.vegetation.Vegetation;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.DataView;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiFunction;

public class GeoDebugCommand extends CommandBase {
    private static final Path DEBUG = Paths.get("debug");
    private static final int RASTER_RADIUS = 64;
    private static final int RASTER_SIZE = RASTER_RADIUS * 2 + 1;

    @Override
    public String getName() {
        return "geodebug";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
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
            ContainerUi.Builder builder = ContainerUi.builder(player)
                    .withTitle(DeferredTranslator.translate(player, new TextComponentTranslation("container.earth.geodebug.name")));

            builder.withElement(Items.FILLED_MAP, "Make Rasters", () -> this.openRasterUi(player));

            ContainerUi ui = builder.build();
            player.displayGUIChest(ui.createInventory());
        } else {
            throw DeferredTranslator.createException(player, "commands.earth.wrong_world");
        }
    }

    private void openRasterUi(EntityPlayerMP player) {
        ContainerUi.Builder builder = ContainerUi.builder(player)
                .withTitle(DeferredTranslator.translate(player, new TextComponentTranslation("container.earth.geodebug.name")));

        builder.withElement(Item.getItemFromBlock(Blocks.SAPLING), "Vegetation Suitability Index", () -> {
            this.writeRasters(player,
                    this.vegetation("acacia", Trees.ACACIA),
                    this.vegetation("birch", Trees.BIRCH),
                    this.vegetation("oak", Trees.OAK),
                    this.vegetation("jungle", Trees.JUNGLE),
                    this.vegetation("spruce", Trees.SPRUCE),
                    this.vegetation("pine", Trees.PINE)
            );
        });

        builder.withElement(Items.CLAY_BALL, "Soil", () -> {
            this.writeRasters(player,
                    this.variable("clay_content", EarthDataKeys.CLAY_CONTENT),
                    this.variable("silt_content", EarthDataKeys.SILT_CONTENT),
                    this.variable("sand_content", EarthDataKeys.SAND_CONTENT)
            );
        });

        ContainerUi ui = builder.build();
        player.displayGUIChest(ui.createInventory());
    }

    private RasterProvider vegetation(String name, Vegetation vegetation) {
        return new RasterProvider(name).function((dataCache, view) -> {
            GrowthIndicator indicator = vegetation.getGrowthIndicator();

            GrowthPredictors predictors = new GrowthPredictors();
            GrowthPredictors.Sampler predictorSampler = GrowthPredictors.sampler();

            UByteRaster raster = UByteRaster.create(view);

            for (int y = 0; y < view.getHeight(); y++) {
                for (int x = 0; x < view.getWidth(); x++) {
                    int blockX = view.getMinX() + x;
                    int blockZ = view.getMinY() + y;
                    predictorSampler.sampleTo(dataCache, blockX, blockZ, predictors);

                    if (predictors.elevation >= 0.0) {
                        double suitabilityIndex = indicator.evaluate(predictors);
                        raster.set(x, y, MathHelper.floor(suitabilityIndex * 255.0));
                    }
                }
            }

            return raster;
        });
    }

    private RasterProvider variable(String name, DataKey<UByteRaster> key) {
        return new RasterProvider(name).function((dataCache, view) -> {
            UByteRaster.Sampler sampler = UByteRaster.sampler(key);
            return sampler.sample(dataCache, view);
        });
    }

    private void writeRasters(EntityPlayerMP player, RasterProvider... providers) {
        TerrariumWorld terrarium = TerrariumWorld.get(player.world);
        Preconditions.checkNotNull(terrarium, "terrarium world data was null");

        ColumnDataCache dataCache = terrarium.getDataCache();

        int x = MathHelper.floor(player.posX);
        int z = MathHelper.floor(player.posZ);

        DataView view = DataView.rect(x - RASTER_RADIUS, z - RASTER_RADIUS, RASTER_SIZE, RASTER_SIZE);

        for (RasterProvider provider : providers) {
            UByteRaster raster = provider.function.apply(dataCache, view);
            this.writeRaster(raster, provider.name);
        }
    }

    private void writeRaster(UByteRaster raster, String name) {
        BufferedImage image = new BufferedImage(raster.getWidth(), raster.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < raster.getHeight(); y++) {
            for (int x = 0; x < raster.getWidth(); x++) {
                int value = raster.get(x, y);
                image.setRGB(x, y, 255 << 16 | value << 8);
            }
        }

        try {
            if (!Files.exists(DEBUG)) Files.createDirectories(DEBUG);
            ImageIO.write(image, "png", DEBUG.resolve(name + ".png").toFile());
        } catch (IOException e) {
            TerrariumEarth.LOGGER.warn("Failed to write raster {}", name, e);
        }
    }

    private static class RasterProvider {
        private final String name;
        private BiFunction<ColumnDataCache, DataView, UByteRaster> function;

        RasterProvider(String name) {
            this.name = name;
        }

        public RasterProvider function(BiFunction<ColumnDataCache, DataView, UByteRaster> function) {
            this.function = function;
            return this;
        }
    }
}
