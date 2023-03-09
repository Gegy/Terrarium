package dev.gegy.terrarium.backend.expr.predictor;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;

import static dev.gegy.terrarium.backend.expr.predictor.Predictors.*;

@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 10, time = 10)
@Fork(2)
public class PredictorBench {
    @State(Scope.Benchmark)
    public static class Parameters {
        public float a = 1.0f;
        public float b = 2.0f;
        public float c = 3.0f;
    }

    @State(Scope.Benchmark)
    public static class BenchPredictors {
        public Predictor<Parameters> handWrittenPredictor;
        public Predictor<Parameters> compiledPredictor;

        @Setup
        public void setup() {
            handWrittenPredictor = parameters -> 2.0f * ((parameters.a + (parameters.b * parameters.b)) - parameters.c);
            compiledPredictor = PredictorNode.compile(mul(
                    constant(2.0f),
                    sub(
                            add(
                                    opaque(p -> p.a),
                                    mul(opaque(p -> p.b), opaque(p -> p.b))
                            ),
                            opaque(p -> p.c)
                    )
            ));
        }
    }

    @Benchmark
    public float handWritten(final BenchPredictors predictors, final Parameters parameters) {
        return predictors.handWrittenPredictor.evaluate(parameters);
    }

    @Benchmark
    public float compiled(final BenchPredictors predictors, final Parameters parameters) {
        return predictors.compiledPredictor.evaluate(parameters);
    }

    public static void main(final String[] args) throws IOException {
        Main.main(args);
    }
}