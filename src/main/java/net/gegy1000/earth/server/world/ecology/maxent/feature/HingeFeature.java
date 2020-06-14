package net.gegy1000.earth.server.world.ecology.maxent.feature;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class HingeFeature implements MaxentFeature {
    public final MaxentFeature parent;
    public final float lambda;
    public final float hinge;
    public final float max;

    public HingeFeature(MaxentFeature parent, float lambda, float hinge, float max) {
        this.parent = parent;
        this.lambda = lambda;
        this.hinge = hinge;
        this.max = max;
    }

    @Override
    public void emitBytecode(MethodVisitor visitor) {
        // if (parent <= hinge) return 0.0;
        // return lambda * (parent - hinge) / (max - hinge);

        this.parent.emitBytecode(visitor);

        // parent - hinge
        visitor.visitLdcInsn(this.hinge);
        visitor.visitInsn(Opcodes.FSUB);

        // x <= 0.0
        visitor.visitInsn(Opcodes.DUP);
        visitor.visitInsn(Opcodes.FCONST_0);
        visitor.visitInsn(Opcodes.FCMPG);

        Label pass = new Label();
        Label miss = new Label();
        Label end = new Label();

        // if (x > 0.0) pass();
        // else miss();
        visitor.visitJumpInsn(Opcodes.IFGT, pass);

        // miss: 0.0
        visitor.visitLabel(miss);
        visitor.visitInsn(Opcodes.POP);
        visitor.visitInsn(Opcodes.FCONST_0);
        visitor.visitJumpInsn(Opcodes.GOTO, end);

        // pass: lambda * (parent - hinge) / (max - hinge);
        visitor.visitLabel(pass);

        // lambda * x / (max - hinge)
        visitor.visitLdcInsn(this.lambda / (this.max - this.hinge));
        visitor.visitInsn(Opcodes.FMUL);

        visitor.visitLabel(end);
    }
}
