package dev.gegy.terrarium.registry;

import dev.gegy.terrarium.backend.expr.ExprCaptures;
import dev.gegy.terrarium.backend.expr.classifier.ClassifierNode;
import net.minecraft.core.Holder;
import org.objectweb.asm.MethodVisitor;

import java.util.function.Consumer;

public record HolderClassifierNode<T, R>(Holder<ClassifierNode<T, R>> holder) implements ClassifierNode<T, R> {
    @Override
    public void compile(final MethodVisitor method, final ExprCaptures captures) {
        holder.value().compile(method, captures);
    }

    @Override
    public void forEachPossibleValue(final Consumer<R> consumer) {
        holder.value().forEachPossibleValue(consumer);
    }

    @Override
    public ClassifierType type() {
        return ClassifierType.OPAQUE;
    }
}
