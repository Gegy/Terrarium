package dev.gegy.terrarium.backend.expr.predictor;

import dev.gegy.terrarium.backend.expr.ExprCaptures;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

record MaxPredictor<T>(PredictorNode<T> left, PredictorNode<T> right) implements PredictorNode<T> {
    @Override
    public void compile(final MethodVisitor method, final ExprCaptures captures) {
        left.compile(method, captures);
        right.compile(method, captures);
        method.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Math.class), "max", Type.getMethodDescriptor(Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE), false);
    }
}
