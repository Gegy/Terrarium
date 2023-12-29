package dev.gegy.terrarium.backend.expr.predictor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.expr.ExprCaptures;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

record SubPredictor<T>(PredictorNode<T> left, PredictorNode<T> right) implements PredictorNode<T> {
    public static <T> MapCodec<SubPredictor<T>> createCodec(final Codec<PredictorNode<T>> subCodec) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                subCodec.fieldOf("left").forGetter(SubPredictor::left),
                subCodec.fieldOf("right").forGetter(SubPredictor::right)
        ).apply(i, SubPredictor::new));
    }

    @Override
    public void compile(final MethodVisitor method, final ExprCaptures captures) {
        if (left.equals(ConstPredictor.zero())) {
            right.compile(method, captures);
            method.visitInsn(Opcodes.FNEG);
            return;
        }
        left.compile(method, captures);
        right.compile(method, captures);
        method.visitInsn(Opcodes.FSUB);
    }

    @Override
    public PredictorType type() {
        return PredictorType.SUB;
    }
}
