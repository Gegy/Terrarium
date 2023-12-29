package dev.gegy.terrarium.backend.expr.predictor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.expr.ExprCaptures;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

record ConstPredictor<T>(float value) implements PredictorNode<T> {
    private static final ConstPredictor<?> ZERO = new ConstPredictor<>(0.0f);
    private static final ConstPredictor<?> ONE = new ConstPredictor<>(1.0f);

    public static <T> MapCodec<ConstPredictor<T>> createCodec(final Codec<PredictorNode<T>> subCodec) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.FLOAT.fieldOf("value").forGetter(ConstPredictor::value)
        ).apply(i, ConstPredictor::new));
    }

    public static <T> Codec<ConstPredictor<T>> createInlineCodec() {
        return Codec.FLOAT.xmap(ConstPredictor::new, ConstPredictor::value);
    }

    @SuppressWarnings("unchecked")
    public static <T> ConstPredictor<T> zero() {
        return (ConstPredictor<T>) ZERO;
    }

    @SuppressWarnings("unchecked")
    public static <T> ConstPredictor<T> one() {
        return (ConstPredictor<T>) ONE;
    }

    @Override
    public void compile(final MethodVisitor method, final ExprCaptures captures) {
        if (value == 0.0f) {
            method.visitInsn(Opcodes.FCONST_0);
        } else if (value == 1.0f) {
            method.visitInsn(Opcodes.FCONST_1);
        } else {
            method.visitLdcInsn(value);
        }
    }

    @Override
    public PredictorType type() {
        return PredictorType.CONST;
    }
}
