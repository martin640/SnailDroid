package sk.martin64.snaildroid.tests;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import sk.martin64.snaildroid.view.MeasureGraphView;
import sk.martin64.snaildroid.view.Utils;

public class ArrayAllocationTest implements TestBase {

    private int a;
    private long started = 0;
    private DummyOutputStream baos;
    private DefaultLongGraphAdapterImpl adapter = new DefaultLongGraphAdapterImpl();

    public ArrayAllocationTest(int a) {
        this.a = a;
    }

    @Override
    public int run() {
        baos = new DummyOutputStream();

        if (a == 1) return runA(false);
        if (a == 2) return runA(true);
        if (a == 3) return runB();
        return CODE_BAD_REQUEST;
    }

    private int runA(boolean a) {
        started = System.nanoTime();
        while (!Thread.interrupted()) {
            byte[] buffer = new byte[8192];
            if (a) Arrays.fill(buffer, Byte.MAX_VALUE);
            try {
                baos.write(buffer);
            } catch (IOException e) {
                return CODE_EXCEPTION;
            }
        }
        return CODE_OK;
    }

    private int runB() {
        started = System.nanoTime();
        byte[] buffer = new byte[8192];
        Random random = new Random();
        while (!Thread.interrupted()) {
            Arrays.fill(buffer, (byte) random.nextInt());
            try {
                baos.write(buffer);
            } catch (IOException e) {
                return CODE_EXCEPTION;
            }
        }
        return CODE_OK;
    }

    @Override
    public String getSpeed(int unit, int x) {
        long s = baos.getLength();
        long est = System.nanoTime() - started;
        double estSec = est / 1000000000d;
        long speed = estSec > 0 ? (long) (s / estSec) : 0;

        adapter.addPoint(x, speed);

        if (unit == UNIT_BYTE)
            return String.format("%s/s", Utils.humanReadableByteCountSI(speed, 2));
        else if (unit == UNIT_BIT)
            return String.format("%s/s", Utils.humanReadableBitsCount(speed, 2));
        else return null;
    }

    @Override
    public MeasureGraphView.GraphAdapter<?> getGraphAdapter() {
        return adapter;
    }

    @Override
    public long getTimeStarted() {
        return started / 1000000L;
    }

    @Override
    public long getDataUsed() {
        return baos.getLengthTotal();
    }

    @Override
    public String getName() {
        return "Java array allocation";
    }
}