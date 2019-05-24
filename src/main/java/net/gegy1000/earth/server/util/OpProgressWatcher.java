package net.gegy1000.earth.server.util;

public interface OpProgressWatcher {
    OpProgressWatcher VOID = new OpProgressWatcher() {
        @Override
        public void notifyProgress(double percentage) {
        }

        @Override
        public void notifyComplete() {
        }

        @Override
        public void notifyException(Exception e) {
            throw new RuntimeException(e);
        }
    };

    void notifyProgress(double percentage);

    void notifyComplete();

    void notifyException(Exception e);

    default OpProgressWatcher map(Mapper mapper) {
        return new OpProgressWatcher() {
            @Override
            public void notifyProgress(double percentage) {
                OpProgressWatcher.this.notifyProgress(mapper.map(percentage));
            }

            @Override
            public void notifyComplete() {
            }

            @Override
            public void notifyException(Exception e) {
                OpProgressWatcher.this.notifyException(e);
            }
        };
    }

    interface Mapper {
        double map(double percentage);
    }
}
