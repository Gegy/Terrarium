package net.gegy1000.earth.server.world.ecology.maxent.feature;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class ThresholdFeature implements MaxentFeature {
    public final MaxentFeature parent;
    public final float lambda;
    public final float min;
    public final float max;
    public final float threshold;

    public ThresholdFeature(MaxentFeature parent, float lambda, float min, float max, float threshold) {
        this.parent = parent;
        this.lambda = lambda;
        this.min = min;
        this.max = max;
        this.threshold = threshold;
    }

    @Override
    public void emitBytecode(MethodVisitor visitor) {
        // lambda * (parent >= threshold ? max : min)

        this.parent.emitBytecode(visitor);

        // x > threshold
        visitor.visitLdcInsn(this.threshold);
        visitor.visitInsn(Opcodes.FCMPG);

        Label max = new Label();
        Label min = new Label();
        Label end = new Label();

        // if (x >= threshold) max();
        // else min();
        visitor.visitJumpInsn(Opcodes.IFGE, max);

        // lambda * min
        visitor.visitLabel(min);
        visitor.visitLdcInsn(this.lambda * this.min);
        visitor.visitJumpInsn(Opcodes.GOTO, end);

        // lambda * max
        visitor.visitLabel(max);
        visitor.visitLdcInsn(this.lambda * this.max);

        visitor.visitLabel(end);
    }
}
