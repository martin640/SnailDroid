package sk.martin64.snaildroid.tests;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sk.martin64.snaildroid.tests.adapters.DefaultLongGraphAdapterImpl;
import sk.martin64.snaildroid.view.MeasureGraphView;
import sk.martin64.snaildroid.view.Utils;

public class ThreadsSpawningTest implements TestBase {

    private long started = 0;
    private DummyOutputStream baos;
    private DefaultLongGraphAdapterImpl adapter = new DefaultLongGraphAdapterImpl();
    private ExecutorService service;

    @Override
    public int run() {
        baos = new DummyOutputStream();
        service = Executors.newCachedThreadPool();

        started = System.nanoTime();
        while (!Thread.interrupted()) {
            service.submit(() -> {
                try {
                    baos.write(0);
                } catch (IOException ignored) {}
            });
        }
        service.shutdown();
        return CODE_OK;
    }

    @Override
    public String getSpeed(int unit, int x) {
        long s = baos.getLength();
        long est = System.nanoTime() - started;
        double estSec = est / 1000000000d;
        long speed = estSec > 0 ? (long) (s / estSec) : 0;

        adapter.addPoint(x, speed);

        return String.format("%s threads/s", Utils.shortNumber(speed, 1));
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
        return 0;
    }

    @Override
    public CharSequence getResultData() {
        return String.format("%s threads spawned", Utils.shortNumber(baos.getLengthTotal(), 1));
    }

    @Override
    public String getName() {
        return "Threads spawning speed";
    }
}