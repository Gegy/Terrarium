package net.gegy1000.earth.server.command;

import com.google.common.base.Preconditions;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.vegetation.Trees;
import net.gegy1000.earth.server.world.ecology.vegetation.Vegetation;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
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

public class GeoDebugCommand extends CommandBase {
    private static final Path DEBUG = Paths.get("debug");

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

            builder.withElement(Item.getItemFromBlock(Blocks.SAPLING), "Evaluate Vegetation", () -> this.evaluateVegetation(player, earth));

            ContainerUi ui = builder.build();
            player.displayGUIChest(ui.createInventory());
        } else {
            throw DeferredTranslator.createException(player, "commands.earth.wrong_world");
        }
    }

    private void evaluateVegetation(EntityPlayerMP player, EarthWorld earth) {
        TerrariumWorld terrarium = TerrariumWorld.get(player.world);
        Preconditions.checkNotNull(terrarium, "terrarium world data was null");

        ColumnDataCache dataCache = terrarium.getDataCache();

        int radius = 64;
        int x = MathHelper.floor(player.posX);
        int z = MathHelper.floor(player.posZ);

        this.evaluateVegetation(dataCache, x, z, radius, "acacia", Trees.ACACIA);
        this.evaluateVegetation(dataCache, x, z, radius, "birch", Trees.BIRCH);
        this.evaluateVegetation(dataCache, x, z, radius, "oak", Trees.OAK);
        this.evaluateVegetation(dataCache, x, z, radius, "jungle", Trees.JUNGLE);
        this.evaluateVegetation(dataCache, x, z, radius, "spruce", Trees.SPRUCE);
        this.evaluateVegetation(dataCache, x, z, radius, "pine", Trees.PINE);
    }

    private void evaluateVegetation(ColumnDataCache dataCache, int originX, int originZ, int radius, String name, Vegetation vegetation) {
        int size = radius * 2 + 1;

        BufferedImage output = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        GrowthIndicator indicator = vegetation.getGrowthIndicator();

        GrowthPredictors predictors = new GrowthPredictors();
        GrowthPredictors.Sampler predictorSampler = GrowthPredictors.sampler();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int blockX = originX + x - radius;
                int blockZ = originZ + y - radius;
                predictorSampler.sampleTo(dataCache, blockX, blockZ, predictors);

                if (predictors.elevation >= 0.0) {
                    double result = indicator.evaluate(predictors);
                    int color = MathHelper.floor(result * 255.0) & 0xFF;

                    output.setRGB(x, y, 255 << 16 | color << 8);
                }
            }
        }

        try {
            if (!Files.exists(DEBUG)) Files.createDirectories(DEBUG);

            ImageIO.write(output, "png", DEBUG.resolve(name + ".png").toFile());
        } catch (IOException e) {
            TerrariumEarth.LOGGER.warn("Failed to write vegetation eval", e);
        }
    }
}
