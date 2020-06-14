package net.gegy1000.earth.server.world.ecology.maxent.feature;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public final class EqualFeature implements MaxentFeature {
    public final MaxentFeature parent;
    public final float lambda;
    public final float min;
    public final float max;
    public final float eq;

    public EqualFeature(MaxentFeature parent, float lambda, float min, float max, float eq) {
        this.parent = parent;
        this.lambda = lambda;
        this.min = min;
        this.max = max;
        this.eq = eq;
    }

    @Override
    public void emitBytecode(MethodVisitor visitor) {
        // lambda * (Math.abs(value - eq) < 1e-2 ? max : min)

        this.parent.emitBytecode(visitor);

        // value - eq
        visitor.visitLdcInsn(this.eq);
        visitor.visitInsn(Opcodes.FSUB);

        // Math.abs(x)
        visitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Math.class), "abs", "(F)F", false);

        // x > 1e-2
        visitor.visitLdcInsn(1e-2F);
        visitor.visitInsn(Opcodes.FCMPG);

        Label eq = new Label();
        Label ne = new Label();
        Label end = new Label();

        // if (x >= 1e-2) ne();
        // else eq();
        visitor.visitJumpInsn(Opcodes.IFGE, ne);

        // lambda * max
        visitor.visitLabel(eq);
        visitor.visitLdcInsn(this.lambda * this.max);
        visitor.visitJumpInsn(Opcodes.GOTO, end);

        // lambda * min
        visitor.visitLabel(ne);
        visitor.visitLdcInsn(this.lambda * this.min);

        visitor.visitLabel(end);
    }
}
