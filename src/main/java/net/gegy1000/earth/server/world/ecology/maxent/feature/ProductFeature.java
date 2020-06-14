package net.gegy1000.earth.server.world.ecology.maxent.feature;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class ProductFeature implements MaxentFeature {
    public final MaxentFeature left;
    public final MaxentFeature right;
    public final float lambda;
    public final float min;
    public final float max;

    public ProductFeature(MaxentFeature left, MaxentFeature right, float lambda, float min, float max) {
        this.left = left;
        this.right = right;
        this.lambda = lambda;
        this.min = min;
        this.max = max;
    }

    @Override
    public void emitBytecode(MethodVisitor visitor) {
        // lambda * (left * right - min) / (max - min)

        this.left.emitBytecode(visitor);
        this.right.emitBytecode(visitor);

        // left * right
        visitor.visitInsn(Opcodes.FMUL);

        // x - min
        visitor.visitLdcInsn(this.min);
        visitor.visitInsn(Opcodes.FSUB);

        // lambda * x / (max - min)
        visitor.visitLdcInsn(this.lambda / (this.max - this.min));
        visitor.visitInsn(Opcodes.FMUL);
    }
}
