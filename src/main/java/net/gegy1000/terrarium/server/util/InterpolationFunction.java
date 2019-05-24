package net.gegy1000.terrarium.server.util;

public enum InterpolationFunction {
    LINEAR(2, 1, 0) {
        @Override
        protected double[] getBuffer() {
            return BUFFER_2.get();
        }

        @Override
        public double calculateLerp(double[] b, double i) {
            return b[0] + (b[1] - b[0]) * i;
        }
    },
    COSINE(2, 1, 0) {
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
    CUBIC(4, 2, 1) {
        @Override
        protected double[] getBuffer() {
            return BUFFER_4.get();
        }

        @Override
        public double calculateLerp(double[] b, double i) {
            return b[1] + 0.5 * i * (b[2] - b[0] + i * (2.0 * b[0] - 5.0 * b[1] + 4.0 * b[2] - b[3] + i * (3.0 * (b[1] - b[2]) + b[3] - b[0])));
        }
    };

    private static final ThreadLocal<double[]> BUFFER_2 = ThreadLocal.withInitial(() -> new double[2]);
    private static final ThreadLocal<double[]> BUFFER_4 = ThreadLocal.withInitial(() -> new double[4]);

    private final int pointCount;
    private final int forward;
    private final int backward;

    InterpolationFunction(int pointCount, int forward, int backward) {
        this.pointCount = pointCount;
        this.forward = forward;
        this.backward = backward;
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
}
