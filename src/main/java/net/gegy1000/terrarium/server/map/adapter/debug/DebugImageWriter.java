package net.gegy1000.terrarium.server.map.adapter.debug;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.map.adapter.CoastlineAdapter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DebugImageWriter {
    public static final ColorSelector<Integer> COASTLINE = value -> {
        int coastType = value & CoastlineAdapter.COAST_TYPE_MASK;
        switch (coastType) {
            case CoastlineAdapter.FREE_FLOOD:
                return 0x404040;
            case CoastlineAdapter.COAST_UP:
                return 0xFF0000;
            case CoastlineAdapter.COAST_DOWN:
                return 0xFFFF00;
            case CoastlineAdapter.COAST_IGNORE:
                return 0xFFFFFF;
        }
        int landType = value & CoastlineAdapter.LAND_TYPE_MASK;
        switch (landType) {
            case CoastlineAdapter.OCEAN:
                return 0x0000FF;
            case CoastlineAdapter.LAND:
                return 0x00FF00;
            case CoastlineAdapter.COAST:
                return 0x009000;
        }
        return 0;
    };

    public static final boolean ENABLED = false;

    private static final File DEBUG_OUTPUT = new File(".", "terrarium_debug");

    static {
        if (ENABLED && !DEBUG_OUTPUT.exists()) {
            DEBUG_OUTPUT.mkdirs();
        }
    }

    public static void write(String fileName, int[] buffer, ColorSelector<Integer> selector, int width, int height) {
        if (ENABLED) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int sampledValue = buffer[x + y * width];
                    image.setRGB(x, y, selector.getColor(sampledValue));
                }
            }

            try {
                ImageIO.write(image, "png", new File(DEBUG_OUTPUT, fileName + ".png"));
            } catch (IOException e) {
                Terrarium.LOGGER.error("Failed to write debug image {}", fileName, e);
            }
        }
    }

    public interface ColorSelector<T> {
        int getColor(T value);
    }
}
