package dev.gegy.terrarium.backend.expr;

import org.objectweb.asm.MethodVisitor;

public interface ExprNode<T> {
    void compile(MethodVisitor method, ExprCaptures captures);
}
