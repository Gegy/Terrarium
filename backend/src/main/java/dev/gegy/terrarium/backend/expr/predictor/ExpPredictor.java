package dev.gegy.terrarium.backend.expr.predictor;

import dev.gegy.terrarium.backend.expr.ExprCaptures;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

record ExpPredictor<T>(PredictorNode<T> value) implements PredictorNode<T> {
    @Override
    public void compile(final MethodVisitor method, final ExprCaptures captures) {
        value.compile(method, captures);
        method.visitInsn(Opcodes.F2D);
        method.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Math.class), "exp", Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE), false);
        method.visitInsn(Opcodes.D2F);
    }
}
