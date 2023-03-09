package dev.gegy.terrarium.backend.expr;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.constant.ConstantDescs;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class ExprCompiler {
    private static final String PACKAGE = ExprCompiler.class.getPackageName().replace('.', '/');

    private final ExprType exprType;
    private final WeakHashMap<ExprNode<?>, Object> compilationCache = new WeakHashMap<>();

    public ExprCompiler(final ExprType exprType) {
        this.exprType = exprType;
    }

    @SuppressWarnings("unchecked")
    public <T, N extends ExprNode<T>> T compile(final N node) {
        return (T) compilationCache.computeIfAbsent(node, this::doCompile);
    }

    @SuppressWarnings("unchecked")
    private <T, N extends ExprNode<T>> T doCompile(final N node) {
        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        final Type type = Type.getType("L" + PACKAGE + "/CompiledExpression;");
        writer.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC,
                type.getInternalName(),
                null,
                Type.getInternalName(Object.class),
                new String[]{Type.getInternalName(exprType.interfaceType())}
        );

        final List<CaptureDefinition<?>> captures = compileExpressionFunction(node, writer, type);
        final List<Object> classData = compileCaptureInitializers(writer, type, captures);
        compileConstructor(writer);

        final byte[] bytes = writer.toByteArray();

        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup().defineHiddenClassWithClassData(bytes, classData, true);
            final Class<?> expressionClass = lookup.lookupClass();
            final MethodHandle constructor = lookup.findConstructor(expressionClass, MethodType.methodType(void.class));
            return (T) constructor.invoke();
        } catch (final Throwable t) {
            throw new RuntimeException("Failed to define expression", t);
        }
    }

    private <T, N extends ExprNode<T>> List<CaptureDefinition<?>> compileExpressionFunction(final N node, final ClassWriter writer, final Type selfType) {
        final Method interfaceMethod = exprType.interfaceMethod();

        final MethodVisitor method = writer.visitMethod(
                Opcodes.ACC_PUBLIC,
                interfaceMethod.getName(),
                Type.getMethodDescriptor(interfaceMethod),
                null,
                null
        );
        final List<CaptureDefinition<?>> definedCaptures = new ArrayList<>();

        node.compile(method, new ExprCaptures() {
            @Override
            public <I> void load(final MethodVisitor visitor, final I value, final Class<? extends I> type) {
                final CaptureDefinition<?> definition = getOrDefine(value, type);
                visitor.visitFieldInsn(Opcodes.GETSTATIC, selfType.getInternalName(), definition.name(), definition.type().getDescriptor());
            }

            @SuppressWarnings("unchecked")
            private <I> CaptureDefinition<I> getOrDefine(final I value, final Class<? extends I> type) {
                for (final CaptureDefinition<?> definition : definedCaptures) {
                    if (definition.value().equals(value)) {
                        return (CaptureDefinition<I>) definition;
                    }
                }
                final CaptureDefinition<I> definition = new CaptureDefinition<>("v" + definedCaptures.size(), value, type);
                definedCaptures.add(definition);
                return definition;
            }
        });

        final Type returnType = Type.getType(interfaceMethod.getReturnType());
        method.visitInsn(returnType.getOpcode(Opcodes.IRETURN));

        method.visitMaxs(0, 0);

        return definedCaptures;
    }

    private static void compileConstructor(final ClassWriter writer) {
        final MethodVisitor constructor = writer.visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE),
                null,
                null
        );

        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
        constructor.visitInsn(Opcodes.RETURN);

        constructor.visitMaxs(0, 0);
    }

    private static List<Object> compileCaptureInitializers(final ClassWriter writer, final Type type, final List<CaptureDefinition<?>> captures) {
        final MethodVisitor initializer = writer.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "<clinit>",
                Type.getMethodDescriptor(Type.VOID_TYPE),
                null,
                null
        );

        initializer.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(MethodHandles.class), "lookup", Type.getMethodDescriptor(Type.getType(MethodHandles.Lookup.class)), false);
        initializer.visitVarInsn(Opcodes.ASTORE, 0);

        final List<Object> values = new ArrayList<>(captures.size());
        for (int i = 0; i < captures.size(); i++) {
            final CaptureDefinition<?> capture = captures.get(i);
            values.add(capture.value());

            final String descriptor = capture.type().getDescriptor();
            writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, capture.name(), descriptor, null, null);

            initializer.visitVarInsn(Opcodes.ALOAD, 0);
            initializer.visitLdcInsn(ConstantDescs.DEFAULT_NAME);
            initializer.visitLdcInsn(capture.type());
            visitIntConst(initializer, i);
            initializer.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(MethodHandles.class), "classDataAt", Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(MethodHandles.Lookup.class), Type.getType(String.class), Type.getType(Class.class), Type.INT_TYPE), false);
            initializer.visitTypeInsn(Opcodes.CHECKCAST, capture.type().getInternalName());
            initializer.visitFieldInsn(Opcodes.PUTSTATIC, type.getInternalName(), capture.name(), descriptor);
        }

        initializer.visitInsn(Opcodes.RETURN);
        initializer.visitMaxs(0, 0);

        return List.copyOf(values);
    }

    private static void visitIntConst(final MethodVisitor method, final int value) {
        if (value <= 5) {
            method.visitInsn(Opcodes.ICONST_0 + value);
        } else {
            method.visitLdcInsn(value);
        }
    }

    private record CaptureDefinition<T>(String name, T value, Class<? extends T> classType) {
        public Type type() {
            return Type.getType(classType());
        }
    }
}
