package dev.gegy.terrarium.registry;

import dev.gegy.terrarium.backend.expr.ExprCaptures;
import dev.gegy.terrarium.backend.expr.predictor.PredictorNode;
import net.minecraft.core.Holder;
import org.objectweb.asm.MethodVisitor;

public record HolderPredictorNode<T>(Holder<PredictorNode<T>> holder) implements PredictorNode<T> {
    @Override
    public void compile(final MethodVisitor method, final ExprCaptures captures) {
        holder.value().compile(method, captures);
    }

    @Override
    public PredictorType type() {
        return PredictorType.OPAQUE;
    }
}
