package net.gegy1000.terrarium.server.world.generator.customization.widget;

import java.util.function.DoubleUnaryOperator;

public interface SliderScale {
    double apply(double x);

    double reverse(double x);

    static SliderScale linear() {
        return new Builder()
                .apply(x -> x)
                .reverse(x -> x)
                .build();
    }

    static SliderScale power(double power) {
        return new Builder()
                .apply(x -> Math.pow(x, power))
                .reverse(x -> Math.pow(x, 1.0 / power))
                .build();
    }

    final class Builder {
        private DoubleUnaryOperator apply = DoubleUnaryOperator.identity();
        private DoubleUnaryOperator reverse = DoubleUnaryOperator.identity();

        private Builder() {
        }

        public Builder apply(DoubleUnaryOperator op) {
            this.apply = op;
            return this;
        }

        public Builder reverse(DoubleUnaryOperator op) {
            this.reverse = op;
            return this;
        }

        public SliderScale build() {
            return new SliderScale() {
                @Override
                public double apply(double x) {
                    return Builder.this.apply.applyAsDouble(x);
                }

                @Override
                public double reverse(double x) {
                    return Builder.this.reverse.applyAsDouble(x);
                }
            };
        }
    }
}
