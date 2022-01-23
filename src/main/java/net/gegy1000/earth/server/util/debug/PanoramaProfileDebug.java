package net.gegy1000.earth.server.util.debug;

import net.gegy1000.earth.server.world.data.GooglePanorama;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Random;

final class PanoramaProfileDebug {
    private static final int COUNT = 20;

    public static void main(String[] args) throws IOException {
        Random random = new Random(12345);

        System.out.println("loading rasters");
        RasterDebug.Rasters rasters = new RasterDebug.Rasters();

        System.out.println();

        // TODO: higher resolution sampling?

        int count = 0;
        while (count < COUNT) {
            GooglePanorama panorama = trySelectPanorama(random, rasters);

            if (panorama != null) {
                double latitude = panorama.getLatitude();
                double longitude = panorama.getLongitude();

                int x = MathHelper.floor((longitude + 180.0) / 360.0 * rasters.elevation.width());
                int y = MathHelper.floor((90.0 - latitude) / 180.0 * rasters.elevation.height());

                System.out.println((count + 1) + ". " + latitude + ", " + longitude);
                System.out.println("  elevation: " + rasters.elevation.get(x, y) + "m");
                System.out.println("  annual rainfall: " + rasters.annualRainfall.get(x, y) + "mm");
                System.out.println("  min temperature: " + rasters.minTemperature.get(x, y) + "C");
                System.out.println("  mean temperature: " + rasters.meanTemperature.get(x, y) + "C");
                System.out.println("  cover class: " + rasters.cover.get(x, y));
                System.out.println("  soil suborder: " + rasters.soil.get(x, y));
                System.out.println("  soil order: " + rasters.soil.get(x, y).order);
                System.out.println("  organic carbon content: " + rasters.occ.get(x, y) + " g/kg");
                System.out.println("  cation exchange capacity: " + rasters.cec.get(x, y) + " cmolc/kg");
                System.out.println("  pH: " + rasters.ph.get(x, y) / 10.0);
                System.out.println("  clay content: " + rasters.clay.get(x, y) + "%");
                System.out.println("  silt content: " + rasters.silt.get(x, y) + "%");
                System.out.println("  sand content: " + rasters.sand.get(x, y) + "%");

                System.out.println();

                count++;
            }
        }
    }

    @Nullable
    private static GooglePanorama trySelectPanorama(Random random, RasterDebug.Rasters rasters) throws IOException {
        double latitude = random.nextDouble() * 180.0 - 90.0;
        double longitude = random.nextDouble() * 360.0 - 180.0;

        int x = MathHelper.floor((longitude + 180.0) / 360.0 * rasters.elevation.width());
        int y = MathHelper.floor((90.0 - latitude) / 180.0 * rasters.elevation.height());

        float elevation = rasters.elevation.get(x, y);
        if (elevation <= 50.0F) {
            return null;
        }

        return GooglePanorama.lookup(latitude, longitude, 50 * 1000);
    }
}
