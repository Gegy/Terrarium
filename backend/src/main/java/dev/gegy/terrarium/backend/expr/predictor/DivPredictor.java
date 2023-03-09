package dev.gegy.terrarium.backend.expr.predictor;

import dev.gegy.terrarium.backend.expr.ExprCaptures;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

record DivPredictor<T>(PredictorNode<T> left, PredictorNode<T> right) implements PredictorNode<T> {
    @Override
    public void compile(final MethodVisitor method, final ExprCaptures captures) {
        left.compile(method, captures);
        right.compile(method, captures);
        method.visitInsn(Opcodes.FDIV);
    }
}
