package net.gegy1000.earth.server.world.ecology.maxent.feature;

import net.gegy1000.earth.server.world.ecology.maxent.MaxentOutput;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.List;

public final class OutputFeature implements MaxentFeature {
    public final List<MaxentFeature> features;
    private final float densityNormalizer;
    private final float linearPredictorNormalizer;
    private final float entropy;
    private final MaxentOutput output;

    public OutputFeature(
            List<MaxentFeature> features,
            float linearPredictorNormalizer,
            float densityNormalizer,
            float entropy,
            MaxentOutput output
    ) {
        this.features = features;

        this.linearPredictorNormalizer = linearPredictorNormalizer;
        this.densityNormalizer = densityNormalizer;
        this.entropy = entropy;
        this.output = output;
    }

    @Override
    public void emitBytecode(MethodVisitor visitor) {
        this.emitSum(visitor);

        // raw = (Math.exp(sum - linearPredictorNormalizer) / densityNormalizer)

        // sum - linearPredictorNormalizer
        visitor.visitLdcInsn(this.linearPredictorNormalizer);
        visitor.visitInsn(Opcodes.FSUB);

        // Math.exp(x)
        this.emitExp(visitor);

        // x / densityNormalizer
        visitor.visitLdcInsn(this.densityNormalizer);
        visitor.visitInsn(Opcodes.FDIV);

        switch (this.output) {
            case LOGISTIC:
                // 1.0 - 1.0 / (raw * Math.exp(entropy) + 1.0)

                // raw * exp(entropy)
                visitor.visitLdcInsn((float) Math.exp(this.entropy));
                visitor.visitInsn(Opcodes.FMUL);

                // x + 1.0
                visitor.visitInsn(Opcodes.FCONST_1);
                visitor.visitInsn(Opcodes.FADD);

                // 1.0 / x
                visitor.visitInsn(Opcodes.FCONST_1);
                visitor.visitInsn(Opcodes.SWAP);
                visitor.visitInsn(Opcodes.FDIV);

                // 1.0 - x
                visitor.visitInsn(Opcodes.FCONST_1);
                visitor.visitInsn(Opcodes.SWAP);
                visitor.visitInsn(Opcodes.FSUB);

                break;
            case CLOGLOG:
                // 1.0 - Math.exp(raw * -Math.exp(entropy))

                // raw * -exp(entropy)
                visitor.visitLdcInsn((float) -Math.exp(this.entropy));
                visitor.visitInsn(Opcodes.FMUL);

                // Math.exp(x)
                this.emitExp(visitor);

                // 1.0 - x
                visitor.visitInsn(Opcodes.FCONST_1);
                visitor.visitInsn(Opcodes.SWAP);
                visitor.visitInsn(Opcodes.FSUB);

                break;
            case RAW:
            default:
                break;
        }
    }

    private void emitSum(MethodVisitor visitor) {
        if (this.features.isEmpty()) {
            visitor.visitInsn(Opcodes.FCONST_0);
            return;
        }

        // f[0] + f[1] + f[2] + ...
        this.features.get(0).emitBytecode(visitor);

        for (int i = 1; i < this.features.size(); i++) {
            MaxentFeature feature = this.features.get(i);
            feature.emitBytecode(visitor);
            visitor.visitInsn(Opcodes.FADD);
        }
    }

    private void emitExp(MethodVisitor visitor) {
        visitor.visitInsn(Opcodes.F2D);
        visitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Math.class), "exp", "(D)D", false);
        visitor.visitInsn(Opcodes.D2F);
    }
}
