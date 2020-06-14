package net.gegy1000.earth.server.world.ecology.maxent.feature;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class RawFeature implements MaxentFeature {
    public final MaxentFeature parent;
    public final float lambda;
    public final float min;
    public final float max;

    public RawFeature(MaxentFeature parent, float lambda, float min, float max) {
        this.parent = parent;
        this.lambda = lambda;
        this.min = min;
        this.max = max;
    }

    @Override
    public void emitBytecode(MethodVisitor visitor) {
        // lambda * (parent - min) / (max - min)

        this.parent.emitBytecode(visitor);

        if (this.min != 0.0F) {
            // parent - min
            visitor.visitLdcInsn(this.min);
            visitor.visitInsn(Opcodes.FSUB);
        }

        // lambda * x / (max - min)

        visitor.visitLdcInsn(this.lambda / (this.max - this.min));
        visitor.visitInsn(Opcodes.FMUL);
    }
}
