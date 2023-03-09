package dev.gegy.terrarium.backend.expr;

import org.objectweb.asm.MethodVisitor;

public interface ExprCaptures {
    <T> void load(MethodVisitor visitor, T value, Class<? extends T> type);
}
