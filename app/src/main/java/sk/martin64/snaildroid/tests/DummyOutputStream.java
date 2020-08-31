package sk.martin64.snaildroid.tests;

import java.io.IOException;
import java.io.OutputStream;

public class DummyOutputStream extends OutputStream {
    private long length, lengthTotal;

    @Override
    public void write(int i) throws IOException {
        length++;
        lengthTotal++;
    }

    @Override
    public void write(byte[] b) throws IOException {
        length += b.length;
        lengthTotal += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        length += len;
        lengthTotal += len;
    }

    public void reset() {
        length = 0;
    }

    public long getLength() {
        return length;
    }

    public long getLengthTotal() {
        return lengthTotal;
    }
}