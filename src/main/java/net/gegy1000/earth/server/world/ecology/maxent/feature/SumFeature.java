package net.gegy1000.earth.server.world.ecology.maxent.feature;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Collection;

public final class SumFeature implements MaxentFeature {
    public final Collection<MaxentFeature> features;

    public SumFeature(Collection<MaxentFeature> features) {
        this.features = features;
    }

    @Override
    public void emitBytecode(MethodVisitor visitor) {
        visitor.visitInsn(Opcodes.FCONST_0);

        for (MaxentFeature feature : this.features) {
            feature.emitBytecode(visitor);
            visitor.visitInsn(Opcodes.FADD);
        }
    }
}
