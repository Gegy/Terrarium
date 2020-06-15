package net.gegy1000.terrarium.server.util;

public final class Interpolate {
    public static final Interpolate NEAREST = new Interpolate(new Kernel(1), (buffer, x) -> buffer[0]);
    public static final Interpolate LINEAR = new Interpolate(new Kernel(2), (buffer, x) -> {
        return buffer[0] + (buffer[1] - buffer[0]) * x;
    });
    public static final Interpolate COSINE = new Interpolate(new Kernel(2), (buffer, x) -> {
        return LINEAR.evaluate(buffer, cosine(x));
    });
    public static final Interpolate CUBIC = new Interpolate(new Kernel(4).offset(-1), (b, x) -> {
        return b[1] + 0.5 * x * (b[2] - b[0] + x * (2.0 * b[0] - 5.0 * b[1] + 4.0 * b[2] - b[3] + x * (3.0 * (b[1] - b[2]) + b[3] - b[0])));
    });

    private final Kernel kernel;
    private final Function function;

    private Interpolate(Kernel kernel, Function function) {
        this.kernel = kernel;
        this.function = function;
    }

    public static double cosine(double x) {
        return (1.0 - Math.cos(x * Math.PI)) / 2.0;
    }

    public Kernel getKernel() {
        return this.kernel;
    }

    public double evaluate(double[] kernel, double x) {
        return this.function.evaluate(kernel, x);
    }

    public double evaluate(double[][] buffer, double x, double y) {
        double[] verticalSampleBuffer = this.kernel.getBuffer();
        return this.evaluate(buffer, x, y, verticalSampleBuffer);
    }

    public double evaluate(double[][] buffer, double x, double y, double[] tmp) {
        for (int kernelX = 0; kernelX < this.kernel.width; kernelX++) {
            tmp[kernelX] = this.evaluate(buffer[kernelX], y);
        }
        return this.evaluate(tmp, x);
    }

    public static class Kernel {
        private final int width;
        private int offset;

        private final ThreadLocal<double[]> buffer;

        public Kernel(int width) {
            this.width = width;
            this.buffer = ThreadLocal.withInitial(() -> new double[width]);
        }

        public Kernel offset(int offset) {
            this.offset = offset;
            return this;
        }

        public int getWidth() {
            return this.width;
        }

        public int getOffset() {
            return this.offset;
        }

        public double[] getBuffer() {
            return this.buffer.get();
        }
    }

    public interface Function {
        double evaluate(double[] buffer, double x);
    }
}
