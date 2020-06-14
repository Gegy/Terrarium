package net.gegy1000.earth.server.world.ecology.maxent.feature;

import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public final class GetPredictorFeature implements MaxentFeature {
    public final String fieldName;
    public final Class<?> type;

    public GetPredictorFeature(String fieldName, Class<?> type) {
        this.fieldName = fieldName;
        this.type = type;

        if (type != float.class && type != int.class) {
            throw new IllegalArgumentException("wrong predictor feature type");
        }
    }

    @Override
    public void emitBytecode(MethodVisitor visitor) {
        visitor.visitVarInsn(Opcodes.ALOAD, 1);

        String predictorsTy = Type.getInternalName(GrowthPredictors.class);

        if (this.type == float.class) {
            visitor.visitFieldInsn(Opcodes.GETFIELD, predictorsTy, this.fieldName, "F");
        } else if (this.type == int.class) {
            visitor.visitFieldInsn(Opcodes.GETFIELD, predictorsTy, this.fieldName, "I");
            visitor.visitInsn(Opcodes.I2F);
        }
    }
}
