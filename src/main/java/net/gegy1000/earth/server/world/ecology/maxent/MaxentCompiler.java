package net.gegy1000.earth.server.world.ecology.maxent;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.maxent.feature.MaxentFeature;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public final class MaxentCompiler {
    private static final Path DEBUG_ROOT = Paths.get("mods/terrarium/debug/maxent");
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private static String nextClassName() {
        return "CompiledMaxent" + COUNTER.incrementAndGet();
    }

    public static GrowthIndicator compileFeature(MaxentFeature feature) {
        String className = nextClassName();
        byte[] bytes = compileFeatureBytes(className, feature);

        try {
            Class<?> definedClass = defineClass(className, bytes);
            return (GrowthIndicator) definedClass.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to define feature on ClassLoader", e);
        }
    }

    private static Class<?> defineClass(String className, byte[] bytes) throws ClassNotFoundException {
        if (TerrariumEarth.isDeobfuscatedEnvironment()) {
            debugWriteClass(className, bytes);
        }

        ClassLoader classLoader = new ClassLoader(TerrariumEarth.class.getClassLoader()) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.equals(className)) {
                    return super.defineClass(name, bytes, 0, bytes.length);
                }
                return super.loadClass(name);
            }
        };

        return classLoader.loadClass(className);
    }

    private static byte[] compileFeatureBytes(String name, MaxentFeature feature) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        writer.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC,
                name,
                null,
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(GrowthIndicator.class) }
        );

        MethodVisitor constructor = writer.visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null
        );

        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);
        constructor.visitInsn(Opcodes.RETURN);

        // auto-compute
        constructor.visitMaxs(0, 0);

        MethodVisitor evaluate = writer.visitMethod(
                Opcodes.ACC_PUBLIC,
                "evaluate",
                "(" + Type.getDescriptor(GrowthPredictors.class) + ")F",
                null,
                null
        );

        feature.emitBytecode(evaluate);
        evaluate.visitInsn(Opcodes.FRETURN);

        // auto-compute
        evaluate.visitMaxs(0, 0);

        return writer.toByteArray();
    }

    private static void debugWriteClass(String className, byte[] bytes) {
        try {
            if (!Files.exists(DEBUG_ROOT)) {
                Files.createDirectories(DEBUG_ROOT);
            }

            Path path = DEBUG_ROOT.resolve(className + ".class");
            Files.write(path, bytes);
        } catch (IOException e) {
            TerrariumEarth.LOGGER.warn("Failed to write compiled maxent class", e);
        }
    }
}
