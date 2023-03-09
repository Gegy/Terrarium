package dev.gegy.terrarium.backend.expr.predictor;

import dev.gegy.terrarium.backend.expr.ExprCaptures;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

record ConstPredictor<T>(float value) implements PredictorNode<T> {
    private static final ConstPredictor<?> ZERO = new ConstPredictor<>(0.0f);
    private static final ConstPredictor<?> ONE = new ConstPredictor<>(1.0f);

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
}
