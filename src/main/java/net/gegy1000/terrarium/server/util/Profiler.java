package net.gegy1000.terrarium.server.util;

public interface Profiler {
    Profiler VOID = new Profiler() {
        private final Handle handle = new Handle(this);

        @Override
        public Handle push(String name) {
            return this.handle;
        }

        @Override
        public void pop() {
        }
    };

    Handle push(String name);

    void pop();

    final class Handle implements AutoCloseable {
        private final Profiler profiler;

        public Handle(Profiler profiler) {
            this.profiler = profiler;
        }

        @Override
        public void close() {
            this.profiler.pop();
        }
    }
}
