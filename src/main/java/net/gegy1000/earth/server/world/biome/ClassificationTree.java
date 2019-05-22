package net.gegy1000.earth.server.world.biome;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class ClassificationTree<T, C> {
    private final Collection<Node<T, C>> nodes = new ArrayList<>();

    private ClassificationTree() {
    }

    public static <T, C> ClassificationTree<T, C> create() {
        return new ClassificationTree<>();
    }

    public When<ClassificationTree<T, C>, T, C> when(Predicate<C> when) {
        Node<T, C> node = new Node<>(when);
        this.nodes.add(node);
        return new When<>(this, node);
    }

    public When<ClassificationTree<T, C>, T, C> otherwise() {
        return this.when(c -> true);
    }

    @Nullable
    public T classify(C ctx) {
        for (Node<T, C> node : this.nodes) {
            T result = node.classify(ctx);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static class Node<T, C> {
        private final Collection<Node<T, C>> children = new ArrayList<>();
        private final Predicate<C> when;

        private T yield;

        Node(Predicate<C> when) {
            this.when = when;
        }

        public When<Node<T, C>, T, C> when(Predicate<C> when) {
            if (this.yield != null) {
                throw new IllegalStateException("Cannot have children nodes and yield");
            }
            Node<T, C> node = new Node<>(when);
            this.children.add(node);
            return new When<>(this, node);
        }

        public When<Node<T, C>, T, C> otherwise() {
            return this.when(c -> true);
        }

        void yield(T yield) {
            if (!this.children.isEmpty()) {
                throw new IllegalStateException("Cannot yield and have children nodes");
            }
            this.yield = yield;
        }

        T classify(C ctx) {
            if (!this.when.test(ctx)) {
                return null;
            }

            if (this.yield != null) {
                return this.yield;
            }

            for (Node<T, C> node : this.children) {
                T result = node.classify(ctx);
                if (result != null) {
                    return result;
                }
            }

            return null;
        }
    }

    public static class When<P, T, C> {
        private final P parent;
        private final Node<T, C> node;

        When(P parent, Node<T, C> node) {
            this.parent = parent;
            this.node = node;
        }

        public P then(Consumer<Node<T, C>> then) {
            then.accept(this.node);
            return this.parent;
        }

        public P thenYield(T yield) {
            this.node.yield(yield);
            return this.parent;
        }
    }
}
