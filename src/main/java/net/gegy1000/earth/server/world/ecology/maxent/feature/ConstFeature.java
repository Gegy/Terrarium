package net.gegy1000.earth.server.world.ecology.maxent.feature;

import org.objectweb.asm.MethodVisitor;

public final class ConstFeature implements MaxentFeature {
    public final float value;

    public ConstFeature(float value) {
        this.value = value;
    }

    @Override
    public void emitBytecode(MethodVisitor visitor) {
        visitor.visitLdcInsn(this.value);
    }
}
