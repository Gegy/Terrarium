package net.gegy1000.terrarium.server.util;

import net.minecraft.util.math.MathHelper;

import java.awt.Point;
import java.util.function.Consumer;

public class Interpolation {
    public static double cosine(double origin, double target, double intermediate) {
        double cos = (1.0 - Math.cos(intermediate * Math.PI)) / 2.0;
        return origin * (1.0 - cos) + target * cos;
    }

    public static double linear(double origin, double target, double intermediate) {
        return origin + (target - origin) * intermediate;
    }

    public static void interpolateLine(double originX, double originY, double targetX, double targetY, boolean thick, Consumer<Point> points) {
        Point currentPoint = new Point(MathHelper.floor(originX), MathHelper.floor(originY));

        boolean horizontal = false;

        int deltaX = Math.max(1, Math.abs(MathHelper.floor(targetX) - MathHelper.floor(originX)));
        int deltaY = Math.max(1, Math.abs(MathHelper.floor(targetY) - MathHelper.floor(originY)));

        int signumX = Integer.signum(MathHelper.floor(targetX) - MathHelper.floor(originX));
        int signumY = Integer.signum(MathHelper.floor(targetY) - MathHelper.floor(originY));

        if (deltaY > deltaX) {
            int tmp = deltaX;
            deltaX = deltaY;
            deltaY = tmp;
            horizontal = true;
        }

        double longLength = 2 * deltaY - deltaX;

        for (int i = 0; i <= deltaX; i++) {
            points.accept(new Point(currentPoint));

            while (longLength >= 0) {
                if (horizontal) {
                    currentPoint.x += signumX;
                } else {
                    currentPoint.y += signumY;
                }

                if (thick) {
                    points.accept(new Point(currentPoint));
                }

                longLength -= 2 * deltaX;
            }

            if (horizontal) {
                currentPoint.y += signumY;
            } else {
                currentPoint.x += signumX;
            }

            if (thick) {
                points.accept(new Point(currentPoint));
            }

            longLength += 2 * deltaY;
        }
    }
}
