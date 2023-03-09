package dev.gegy.terrarium.backend.expr.predictor;

import dev.gegy.terrarium.backend.expr.ExprCaptures;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

record StepPredictor<T>(PredictorNode<T> value, PredictorNode<T> edge, PredictorNode<T> min, PredictorNode<T> max) implements PredictorNode<T> {
    @Override
    public void compile(final MethodVisitor method, final ExprCaptures captures) {
        value.compile(method, captures);
        edge.compile(method, captures);
        method.visitInsn(Opcodes.FCMPG);

        final Label ifGreater = new Label();
        final Label ifLess = new Label();
        final Label end = new Label();

        method.visitJumpInsn(Opcodes.IFGE, ifGreater);

        method.visitLabel(ifLess);
        min.compile(method, captures);
        method.visitJumpInsn(Opcodes.GOTO, end);

        method.visitLabel(ifGreater);
        max.compile(method, captures);

        method.visitLabel(end);
    }
}
