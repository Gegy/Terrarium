package dev.gegy.terrarium.backend.expr.predictor;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.gegy.terrarium.backend.expr.ExprNode;
import dev.gegy.terrarium.backend.expr.ExprType;
import dev.gegy.terrarium.backend.util.Util;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public interface PredictorNode<T> extends ExprNode<Predictor<T>> {
    ExprType EXPRESSION_TYPE = new ExprType(Predictor.class);

    static <T> Predictor<T> compile(final PredictorNode<T> node) {
        return EXPRESSION_TYPE.compileUnchecked(node);
    }

    static <T> Codecs<T> createCodecs(final Codec<Predictor<T>> builtinCodec, final UnaryOperator<Codec<PredictorNode<T>>> registryCodecFactory) {
        final AtomicReference<Codec<PredictorNode<T>>> registryOrBuiltinCodec = new AtomicReference<>();
        final Codec<PredictorNode<T>> directCodec = createDirectCodec(Util.lazyCodec(registryOrBuiltinCodec::getPlain));
        final Codec<PredictorNode<T>> registryCodec = registryCodecFactory.apply(directCodec);
        registryOrBuiltinCodec.setPlain(Codec.either(builtinCodec, registryCodec).xmap(
                either -> either.map(Predictors::opaque, Function.identity()),
                node -> {
                    if (node instanceof final OpaquePredictor<T> builtin) {
                        return Either.left(builtin.predictor());
                    }
                    return Either.right(node);
                }
        ));
        return new Codecs<>(directCodec, registryOrBuiltinCodec.getPlain());
    }

    private static <T> Codec<PredictorNode<T>> createDirectCodec(final Codec<PredictorNode<T>> subCodec) {
        final Codec<PredictorNode<T>> typedCodec = createTypedCodec(subCodec);
        final Codec<ConstPredictor<T>> inlineConstCodec = ConstPredictor.createInlineCodec();
        return Codec.either(typedCodec, inlineConstCodec).xmap(
                either -> either.map(Function.identity(), Function.identity()),
                predictor -> {
                    if (predictor instanceof final ConstPredictor<T> constant) {
                        return Either.right(constant);
                    } else {
                        return Either.left(predictor);
                    }
                }
        );
    }

    private static <T> Codec<PredictorNode<T>> createTypedCodec(final Codec<PredictorNode<T>> subCodec) {
        final EnumMap<PredictorType, MapCodec<? extends PredictorNode<T>>> codecs = new EnumMap<>(PredictorType.class);
        for (final PredictorType type : PredictorType.values()) {
            codecs.put(type, type.createCodec(subCodec));
        }
        return PredictorType.CODEC.dispatch(PredictorNode::type, codecs::get);
    }

    PredictorType type();

    enum PredictorType {
        CONST("const", ConstPredictor::createCodec),
        OPAQUE("opaque ", CodecFactory.unsupported("Opaque node")),
        ADD("add", AddPredictor::createCodec),
        SUB("sub", SubPredictor::createCodec),
        MUL("mul", MulPredictor::createCodec),
        DIV("div", DivPredictor::createCodec),
        MAX("max", MaxPredictor::createCodec),
        EQ("eq", EqPredictor::createCodec),
        STEP("step", StepPredictor::createCodec),
        EXP("exp", ExpPredictor::createCodec),
        ;

        public static final Codec<PredictorType> CODEC = Util.stringLookupCodec(values(), t -> t.id);

        private final String id;
        private final CodecFactory codecFactory;

        PredictorType(final String id, final CodecFactory codecFactory) {
            this.id = id;
            this.codecFactory = codecFactory;
        }

        public <T> MapCodec<? extends PredictorNode<T>> createCodec(final Codec<PredictorNode<T>> subCodec) {
            return codecFactory.create(subCodec);
        }
    }

    interface CodecFactory {
        static CodecFactory unsupported(final String name) {
            return new CodecFactory() {
                @Override
                public <T> MapCodec<? extends PredictorNode<T>> create(final Codec<PredictorNode<T>> subCodec) {
                    return Util.unsupportedMapCodec(name);
                }
            };
        }

        <T> MapCodec<? extends PredictorNode<T>> create(Codec<PredictorNode<T>> subCodec);
    }

    record Codecs<T>(Codec<PredictorNode<T>> directCodec, Codec<PredictorNode<T>> externalCodec) {
    }
}
