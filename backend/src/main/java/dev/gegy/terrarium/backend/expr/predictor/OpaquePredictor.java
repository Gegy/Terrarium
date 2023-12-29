package dev.gegy.terrarium.backend.expr.predictor;

import dev.gegy.terrarium.backend.expr.ExprCaptures;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

record OpaquePredictor<T>(Predictor<T> predictor) implements PredictorNode<T> {
    @Override
    public void compile(final MethodVisitor method, final ExprCaptures captures) {
        captures.load(method, predictor, Predictor.class);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getInternalName(Predictor.class), "evaluate", Type.getMethodDescriptor(Type.FLOAT_TYPE, Type.getType(Object.class)), true);
    }

    @Override
    public PredictorType type() {
        return PredictorType.OPAQUE;
    }
}
