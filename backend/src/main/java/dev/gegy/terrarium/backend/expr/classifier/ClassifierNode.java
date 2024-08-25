package dev.gegy.terrarium.backend.expr.classifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.gegy.terrarium.backend.expr.ExprNode;
import dev.gegy.terrarium.backend.expr.ExprType;
import dev.gegy.terrarium.backend.expr.predictor.PredictorNode;
import dev.gegy.terrarium.backend.util.Util;

import java.util.EnumMap;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public interface ClassifierNode<T, R> extends ExprNode<Classifier<T, R>> {
    ExprType EXPRESSION_TYPE = new ExprType(Classifier.class);

    static <T, R> Classifier<T, R> compile(final ClassifierNode<T, R> node) {
        return EXPRESSION_TYPE.compileUnchecked(node);
    }

    static <T, R> Codec<ClassifierNode<T, R>> createCodec(final Codec<PredictorNode<T>> predictorCodec, final Codec<R> resultCodec, final UnaryOperator<Codec<ClassifierNode<T, R>>> registryCodecFactory) {
        return Util.recursiveCodec(subCodec -> {
            final Codec<ClassifierNode<T, R>> registryCodec = registryCodecFactory.apply(subCodec);
            return createTypedCodec(registryCodec, predictorCodec, resultCodec);
        });
    }

    private static <T, R> Codec<ClassifierNode<T, R>> createTypedCodec(final Codec<ClassifierNode<T, R>> subCodec, final Codec<PredictorNode<T>> predictorCodec, final Codec<R> resultCodec) {
        final EnumMap<ClassifierType, MapCodec<? extends ClassifierNode<T, R>>> codecs = new EnumMap<>(ClassifierType.class);
        for (final ClassifierType type : ClassifierType.values()) {
            codecs.put(type, type.createCodec(subCodec, predictorCodec, resultCodec));
        }
        return ClassifierType.CODEC.dispatch(ClassifierNode::type, codecs::get);
    }

    ClassifierType type();

    void forEachPossibleValue(Consumer<R> consumer);

    enum ClassifierType {
        OPAQUE("opaque", CodecFactory.unsupported("Opaque classifier")),
        CONST("const", ConstClassifier::createCodec),
        THRESHOLD("threshold", ThresholdClassifier::createCodec),
        ;

        public static final Codec<ClassifierType> CODEC = Util.stringLookupCodec(values(), t -> t.id);

        private final String id;
        private final CodecFactory codecFactory;

        ClassifierType(final String id, final CodecFactory codecFactory) {
            this.id = id;
            this.codecFactory = codecFactory;
        }

        public <T, R> MapCodec<? extends ClassifierNode<T, R>> createCodec(final Codec<ClassifierNode<T, R>> subCodec, final Codec<PredictorNode<T>> predictorCodec, final Codec<R> resultCodec) {
            return codecFactory.create(subCodec, predictorCodec, resultCodec);
        }
    }

    interface CodecFactory {
        static CodecFactory unsupported(final String name) {
            return new CodecFactory() {
                @Override
                public <T, R> MapCodec<? extends ClassifierNode<T, R>> create(final Codec<ClassifierNode<T, R>> subCodec, final Codec<PredictorNode<T>> predictorCodec, final Codec<R> resultCodec) {
                    return Util.unsupportedMapCodec(name);
                }
            };
        }

        <T, R> MapCodec<? extends ClassifierNode<T, R>> create(Codec<ClassifierNode<T, R>> subCodec, Codec<PredictorNode<T>> predictorCodec, Codec<R> resultCodec);
    }
}
