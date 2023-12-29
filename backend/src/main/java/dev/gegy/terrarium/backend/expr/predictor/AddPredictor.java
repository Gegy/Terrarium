package dev.gegy.terrarium.backend.expr.predictor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.expr.ExprCaptures;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

record AddPredictor<T>(PredictorNode<T> left, PredictorNode<T> right) implements PredictorNode<T> {
    public static <T> MapCodec<AddPredictor<T>> createCodec(final Codec<PredictorNode<T>> subCodec) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                subCodec.fieldOf("left").forGetter(AddPredictor::left),
                subCodec.fieldOf("right").forGetter(AddPredictor::right)
        ).apply(i, AddPredictor::new));
    }

    @Override
    public void compile(final MethodVisitor method, final ExprCaptures captures) {
        if (left.equals(right)) {
            left.compile(method, captures);
            method.visitInsn(Opcodes.DUP);
            method.visitInsn(Opcodes.FADD);
            return;
        }
        left.compile(method, captures);
        right.compile(method, captures);
        method.visitInsn(Opcodes.FADD);
    }

    @Override
    public PredictorType type() {
        return PredictorType.ADD;
    }
}
