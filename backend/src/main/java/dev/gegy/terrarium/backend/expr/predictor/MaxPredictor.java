package dev.gegy.terrarium.backend.expr.predictor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.expr.ExprCaptures;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

record MaxPredictor<T>(PredictorNode<T> left, PredictorNode<T> right) implements PredictorNode<T> {
    public static <T> MapCodec<MaxPredictor<T>> createCodec(final Codec<PredictorNode<T>> subCodec) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                subCodec.fieldOf("left").forGetter(MaxPredictor::left),
                subCodec.fieldOf("right").forGetter(MaxPredictor::right)
        ).apply(i, MaxPredictor::new));
    }

    @Override
    public void compile(final MethodVisitor method, final ExprCaptures captures) {
        left.compile(method, captures);
        right.compile(method, captures);
        method.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Math.class), "max", Type.getMethodDescriptor(Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE), false);
    }

    @Override
    public PredictorType type() {
        return PredictorType.MAX;
    }
}
