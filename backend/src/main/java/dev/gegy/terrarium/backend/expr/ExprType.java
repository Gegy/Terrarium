package dev.gegy.terrarium.backend.expr;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public class ExprType {
    private final Class<?> interfaceType;
    private final Method interfaceMethod;
    private final ExprCompiler compiler = new ExprCompiler(this);

    public ExprType(final Class<?> interfaceType) {
        final List<Method> methods = Arrays.stream(interfaceType.getDeclaredMethods())
                .filter(method -> !method.isDefault() && !Modifier.isStatic(method.getModifiers()))
                .toList();
        if (!interfaceType.isInterface() || methods.size() != 1) {
            throw new IllegalArgumentException("Expression type must be a functional interface: " + interfaceType);
        }
        this.interfaceType = interfaceType;
        interfaceMethod = methods.get(0);
    }

    public Class<?> interfaceType() {
        return interfaceType;
    }

    public Method interfaceMethod() {
        return interfaceMethod;
    }

    public <T, N extends ExprNode<T>> T compileUnchecked(final N node) {
        return compiler.compile(node);
    }
}
