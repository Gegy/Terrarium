package net.gegy1000.earth.server.util;

import java.io.IOException;
import java.io.InputStream;

public class WatchedInputStream extends InputStream {
    private final InputStream input;
    private final OpProgressWatcher watcher;

    private int readBytes;
    private final int totalBytes;

    public WatchedInputStream(InputStream input, OpProgressWatcher watcher) throws IOException {
        this.input = input;
        this.watcher = watcher;
        this.totalBytes = this.input.available();
    }

    @Override
    public int read() throws IOException {
        int read = this.input.read();
        if (read != -1) {
            this.readBytes(1);
        }
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int count = this.input.read(b);
        this.readBytes(count);
        return count;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count = this.input.read(b, off, len);
        this.readBytes(count);
        return count;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = this.input.skip(n);
        this.readBytes((int) skipped);
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return this.input.available();
    }

    @Override
    public void close() throws IOException {
        this.input.close();
    }

    private void readBytes(int count) {
        if (count <= 0) {
            return;
        }

        this.readBytes += count;
        this.watcher.notifyProgress((double) this.readBytes / this.totalBytes);

        if (this.readBytes >= this.totalBytes) {
            this.watcher.notifyComplete();
        }
    }
}
