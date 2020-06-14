package net.gegy1000.earth.server.world.ecology.maxent.feature;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class ReverseHingeFeature implements MaxentFeature {
    public final MaxentFeature parent;
    public final float lambda;
    public final float min;
    public final float hinge;

    public ReverseHingeFeature(MaxentFeature parent, float lambda, float min, float hinge) {
        this.parent = parent;
        this.lambda = lambda;
        this.min = min;
        this.hinge = hinge;
    }

    @Override
    public void emitBytecode(MethodVisitor visitor) {
        // if (parent >= hinge) return 0.0;
        // return lambda * (hinge - parent) / (hinge - min);

        this.parent.emitBytecode(visitor);

        // hinge - parent
        visitor.visitLdcInsn(this.hinge);
        visitor.visitInsn(Opcodes.SWAP);
        visitor.visitInsn(Opcodes.FSUB);

        // x <= 0.0
        visitor.visitInsn(Opcodes.DUP);
        visitor.visitInsn(Opcodes.FCONST_0);
        visitor.visitInsn(Opcodes.FCMPG);

        Label pass = new Label();
        Label miss = new Label();
        Label end = new Label();

        // if (x <= 0.0) miss();
        // else pass();
        visitor.visitJumpInsn(Opcodes.IFGT, pass);

        // miss: 0.0
        visitor.visitLabel(miss);
        visitor.visitInsn(Opcodes.POP);
        visitor.visitInsn(Opcodes.FCONST_0);
        visitor.visitJumpInsn(Opcodes.GOTO, end);

        // pass: lambda * (hinge - parent) / (hinge - min);
        visitor.visitLabel(pass);

        // lambda * x / (max - hinge)
        visitor.visitLdcInsn(this.lambda / (this.hinge - this.min));
        visitor.visitInsn(Opcodes.FMUL);

        visitor.visitLabel(end);
    }
}
