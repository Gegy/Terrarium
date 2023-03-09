package dev.gegy.terrarium.backend.expr.predictor;

import dev.gegy.terrarium.backend.expr.ExprNode;
import dev.gegy.terrarium.backend.expr.ExprType;

public interface PredictorNode<T> extends ExprNode<Predictor<T>> {
    ExprType EXPRESSION_TYPE = new ExprType(Predictor.class);

    static <T> Predictor<T> compile(final PredictorNode<T> node) {
        return EXPRESSION_TYPE.compileUnchecked(node);
    }
}
