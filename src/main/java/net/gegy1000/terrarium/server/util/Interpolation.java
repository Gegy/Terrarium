package net.gegy1000.terrarium.server.util;

import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.minecraft.util.math.MathHelper;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Interpolation {
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

    public enum Method {
        LINEAR("linear", 2, 1, 0) {
            @Override
            protected double[] getBuffer() {
                return BUFFER_2.get();
            }

            @Override
            public double calculateLerp(double[] b, double i) {
                return b[0] + (b[1] - b[0]) * i;
            }
        },
        COSINE("cosine", 2, 1, 0) {
            @Override
            protected double[] getBuffer() {
                return BUFFER_2.get();
            }

            @Override
            public double calculateLerp(double[] b, double i) {
                double easedIntermediate = (1.0 - Math.cos(i * Math.PI)) / 2.0;
                return b[0] * (1.0 - easedIntermediate) + b[1] * easedIntermediate;
            }
        },
        CUBIC("cubic", 4, 2, 1) {
            @Override
            protected double[] getBuffer() {
                return BUFFER_4.get();
            }

            @Override
            public double calculateLerp(double[] b, double i) {
                return b[1] + 0.5 * i * (b[2] - b[0] + i * (2.0 * b[0] - 5.0 * b[1] + 4.0 * b[2] - b[3] + i * (3.0 * (b[1] - b[2]) + b[3] - b[0])));
            }
        };

        private static final Map<String, Method> METHOD_MAPPINGS = new HashMap<>();

        private static final ThreadLocal<double[]> BUFFER_2 = ThreadLocal.withInitial(() -> new double[2]);
        private static final ThreadLocal<double[]> BUFFER_4 = ThreadLocal.withInitial(() -> new double[4]);

        static {
            for (Method method : Method.values()) {
                METHOD_MAPPINGS.put(method.getKey(), method);
            }
        }

        private final String key;
        private final int pointCount;
        private final int forward;
        private final int backward;

        Method(String key, int pointCount, int forward, int backward) {
            this.key = key;
            this.pointCount = pointCount;
            this.forward = forward;
            this.backward = backward;
        }

        public String getKey() {
            return this.key;
        }

        public int getPointCount() {
            return this.pointCount;
        }

        public int getForward() {
            return this.forward;
        }

        public int getBackward() {
            return this.backward;
        }

        protected abstract double[] getBuffer();

        protected abstract double calculateLerp(double[] b, double i);

        public double lerp(double[] buffer, double intermediate) {
            if (this.pointCount != buffer.length) {
                throw new IllegalStateException("This method cannot interpolate with " + buffer.length + " points");
            }
            return this.calculateLerp(buffer, intermediate);
        }

        public double lerp(double origin, double target, double intermediate) {
            double[] buffer = BUFFER_2.get();
            buffer[0] = origin;
            buffer[1] = target;
            return this.lerp(buffer, intermediate);
        }

        public double lerp(double p1, double p2, double p3, double p4, double intermediate) {
            double[] buffer = BUFFER_4.get();
            buffer[0] = p1;
            buffer[1] = p2;
            buffer[2] = p3;
            buffer[3] = p4;
            return this.lerp(buffer, intermediate);
        }

        public double lerp2d(double[][] buffer, double intermediateX, double intermediateY) {
            double[] verticalSampleBuffer = this.getBuffer();
            for (int sampleX = 0; sampleX < this.pointCount; sampleX++) {
                verticalSampleBuffer[sampleX] = this.lerp(buffer[sampleX], intermediateY);
            }
            return this.lerp(verticalSampleBuffer, intermediateX);
        }

        public static Method parse(String key) throws InvalidJsonException {
            if (METHOD_MAPPINGS.containsKey(key)) {
                return METHOD_MAPPINGS.get(key);
            }
            throw new InvalidJsonException("Tried to parse invalid interpolation method type " + key);
        }
    }
}
