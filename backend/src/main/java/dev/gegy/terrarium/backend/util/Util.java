package dev.gegy.terrarium.backend.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class Util {
    public static void readFully(final ReadableByteChannel channel, final ByteBuffer buffer) throws IOException {
        if (!tryReadFully(channel, buffer)) {
            throw new EOFException("Expected " + buffer.capacity() + " bytes, got " + buffer.position());
        }
    }

    public static boolean tryReadFully(final ReadableByteChannel channel, final ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            if (channel.read(buffer) < 0) {
                return false;
            }
        }
        buffer.flip();
        return true;
    }

    public static InputStream asInputStream(final ByteBuffer buffer) {
        return new InputStream() {
            @Override
            public int read() {
                if (!buffer.hasRemaining()) {
                    return -1;
                }
                return buffer.get() & 0xff;
            }

            @Override
            public int read(final byte[] b, final int off, final int len) {
                if (!buffer.hasRemaining()) {
                    return -1;
                }
                final int count = Math.min(len, buffer.remaining());
                buffer.get(b, off, count);
                return count;
            }

            @Override
            public int available() {
                return buffer.remaining();
            }
        };
    }
}
