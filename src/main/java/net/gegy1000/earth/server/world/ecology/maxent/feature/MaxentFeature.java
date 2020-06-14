package net.gegy1000.earth.server.world.ecology.maxent.feature;

import org.objectweb.asm.MethodVisitor;

public interface MaxentFeature {
    void emitBytecode(MethodVisitor visitor);
}
