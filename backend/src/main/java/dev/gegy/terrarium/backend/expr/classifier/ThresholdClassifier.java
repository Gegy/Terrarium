package dev.gegy.terrarium.backend.expr.classifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.expr.ExprCaptures;
import dev.gegy.terrarium.backend.expr.predictor.PredictorNode;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.Consumer;

record ThresholdClassifier<T, R>(PredictorNode<T> value, PredictorNode<T> threshold, ClassifierNode<T, R> ifGreater, ClassifierNode<T, R> ifLess) implements ClassifierNode<T, R> {
    public static <T, R> MapCodec<ThresholdClassifier<T, R>> createCodec(final Codec<ClassifierNode<T, R>> subCodec, final Codec<PredictorNode<T>> predictorCodec, final Codec<R> resultCodec) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                predictorCodec.fieldOf("value").forGetter(ThresholdClassifier::value),
                predictorCodec.fieldOf("threshold").forGetter(ThresholdClassifier::threshold),
                subCodec.fieldOf("if_greater").forGetter(ThresholdClassifier::ifGreater),
                subCodec.fieldOf("if_less").forGetter(ThresholdClassifier::ifLess)
        ).apply(i, ThresholdClassifier::new));
    }

    @Override
    public void compile(final MethodVisitor method, final ExprCaptures captures) {
        value.compile(method, captures);
        threshold.compile(method, captures);
        method.visitInsn(Opcodes.FCMPG);

        final Label ifGreater = new Label();
        final Label ifLess = new Label();
        final Label end = new Label();

        method.visitJumpInsn(Opcodes.IFGE, ifGreater);

        method.visitLabel(ifLess);
        this.ifLess.compile(method, captures);
        method.visitJumpInsn(Opcodes.GOTO, end);

        method.visitLabel(ifGreater);
        this.ifGreater.compile(method, captures);

        method.visitLabel(end);
    }

    @Override
    public ClassifierType type() {
        return ClassifierType.THRESHOLD;
    }

    @Override
    public void forEachPossibleValue(final Consumer<R> consumer) {
        ifGreater.forEachPossibleValue(consumer);
        ifLess.forEachPossibleValue(consumer);
    }
}
