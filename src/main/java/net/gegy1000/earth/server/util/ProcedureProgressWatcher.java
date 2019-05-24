package net.gegy1000.earth.server.util;

public interface ProcedureProgressWatcher {
    ProcedureProgressWatcher VOID = new ProcedureProgressWatcher() {
        @Override
        public OpProgressWatcher startOp(String description) {
            return OpProgressWatcher.VOID;
        }

        @Override
        public void notifyProgress(double percentage) {
        }

        @Override
        public void notifyComplete() {
        }
    };

    OpProgressWatcher startOp(String description);

    void notifyProgress(double percentage);

    void notifyComplete();
}
