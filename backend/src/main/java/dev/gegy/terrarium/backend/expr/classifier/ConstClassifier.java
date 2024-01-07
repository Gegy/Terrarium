package dev.gegy.terrarium.backend.expr.classifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.expr.ExprCaptures;
import dev.gegy.terrarium.backend.expr.predictor.PredictorNode;
import org.objectweb.asm.MethodVisitor;

import java.util.function.Consumer;

record ConstClassifier<T, R>(R value) implements ClassifierNode<T, R> {
    public static <T, R> MapCodec<ConstClassifier<T, R>> createCodec(final Codec<ClassifierNode<T, R>> subCodec, final Codec<PredictorNode<T>> predictorCodec, final Codec<R> resultCodec) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                resultCodec.fieldOf("value").forGetter(ConstClassifier::value)
        ).apply(i, ConstClassifier::new));
    }

    @Override
    public void compile(final MethodVisitor method, final ExprCaptures captures) {
        // Actual type is erased due to generics
        captures.load(method, value, Object.class);
    }

    @Override
    public ClassifierType type() {
        return ClassifierType.CONST;
    }

    @Override
    public void forEachPossibleValue(final Consumer<R> consumer) {
        consumer.accept(value);
    }
}
