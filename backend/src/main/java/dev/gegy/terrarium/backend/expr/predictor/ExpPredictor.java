package dev.gegy.terrarium.backend.expr.predictor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.expr.ExprCaptures;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

record ExpPredictor<T>(PredictorNode<T> value) implements PredictorNode<T> {
    public static <T> MapCodec<ExpPredictor<T>> createCodec(final Codec<PredictorNode<T>> subCodec) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                subCodec.fieldOf("value").forGetter(ExpPredictor::value)
        ).apply(i, ExpPredictor::new));
    }

    @Override
    public void compile(final MethodVisitor method, final ExprCaptures captures) {
        value.compile(method, captures);
        method.visitInsn(Opcodes.F2D);
        method.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Math.class), "exp", Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE), false);
        method.visitInsn(Opcodes.D2F);
    }

    @Override
    public PredictorType type() {
        return PredictorType.EXP;
    }
}
