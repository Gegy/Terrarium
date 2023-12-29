package dev.gegy.terrarium.backend.expr.predictor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.expr.ExprCaptures;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

record EqPredictor<T>(PredictorNode<T> left, PredictorNode<T> right, PredictorNode<T> ifEqual, PredictorNode<T> ifNotEqual, float epsilon) implements PredictorNode<T> {
    private static final float DEFAULT_EPSILON = 0.01f;

    public static <T> MapCodec<EqPredictor<T>> createCodec(final Codec<PredictorNode<T>> subCodec) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                subCodec.fieldOf("left").forGetter(EqPredictor::left),
                subCodec.fieldOf("right").forGetter(EqPredictor::right),
                subCodec.fieldOf("if_equal").forGetter(EqPredictor::ifEqual),
                subCodec.fieldOf("if_not_equal").forGetter(EqPredictor::ifNotEqual),
                Codec.FLOAT.optionalFieldOf("epsilon", DEFAULT_EPSILON).forGetter(EqPredictor::epsilon)
        ).apply(i, EqPredictor::new));
    }

    public EqPredictor(final PredictorNode<T> left, final PredictorNode<T> right, final PredictorNode<T> ifEqual, final PredictorNode<T> ifNotEqual) {
        this(left, right, ifEqual, ifNotEqual, DEFAULT_EPSILON);
    }

    @Override
    public void compile(final MethodVisitor method, final ExprCaptures captures) {
        left.compile(method, captures);

        if (!right.equals(ConstPredictor.zero())) {
            right.compile(method, captures);
            method.visitInsn(Opcodes.FSUB);
        }

        method.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Math.class), "abs", Type.getMethodDescriptor(Type.FLOAT_TYPE, Type.FLOAT_TYPE), false);

        method.visitLdcInsn(epsilon);
        method.visitInsn(Opcodes.FCMPG);

        final Label ifEqual = new Label();
        final Label ifNotEqual = new Label();
        final Label end = new Label();

        method.visitJumpInsn(Opcodes.IFGE, ifNotEqual);

        method.visitLabel(ifEqual);
        this.ifEqual.compile(method, captures);
        method.visitJumpInsn(Opcodes.GOTO, end);

        method.visitLabel(ifNotEqual);
        this.ifNotEqual.compile(method, captures);

        method.visitLabel(end);
    }

    @Override
    public PredictorType type() {
        return PredictorType.EQ;
    }
}
