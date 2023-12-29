package dev.gegy.terrarium.backend.expr.predictor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.expr.ExprCaptures;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

record DivPredictor<T>(PredictorNode<T> left, PredictorNode<T> right) implements PredictorNode<T> {
    public static <T> MapCodec<DivPredictor<T>> createCodec(final Codec<PredictorNode<T>> subCodec) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                subCodec.fieldOf("left").forGetter(DivPredictor::left),
                subCodec.fieldOf("right").forGetter(DivPredictor::right)
        ).apply(i, DivPredictor::new));
    }

    @Override
    public void compile(final MethodVisitor method, final ExprCaptures captures) {
        left.compile(method, captures);
        right.compile(method, captures);
        method.visitInsn(Opcodes.FDIV);
    }

    @Override
    public PredictorType type() {
        return PredictorType.DIV;
    }
}
