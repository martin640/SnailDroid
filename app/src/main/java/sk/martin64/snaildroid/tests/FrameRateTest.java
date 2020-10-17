package sk.martin64.snaildroid.tests;

import android.view.ViewGroup;

import sk.martin64.snaildroid.tests.adapters.DefaultIntGraphAdapterImpl;
import sk.martin64.snaildroid.view.FrameRateDummyView;
import sk.martin64.snaildroid.view.MeasureGraphView;
import sk.martin64.snaildroid.view.Utils;

public class FrameRateTest implements TestBase {

    public FrameRateTest(ViewGroup v) {
        v.addView(view = new FrameRateDummyView(v.getContext()));
    }

    private FrameRateDummyView view;
    private long started = 0;
    private DefaultIntGraphAdapterImpl adapter = new DefaultIntGraphAdapterImpl();

    @Override
    public int run() {
        started = System.currentTimeMillis();
        view.post(() -> view.startMeasure());

        while (!Thread.interrupted()) { } // block current thread until test end

        view.post(() -> view.stopMeasure());
        return CODE_OK;
    }

    @Override
    public String getSpeed(int unit, int x) {
        long frames = view.getFramesTotal();
        long time = System.currentTimeMillis() - view.getStartTime();
        double estSec = time / 1000d;
        int speed = estSec > 0 ? (int) (frames / estSec) : 0;

        adapter.addPoint(x, speed);
        return speed + " FPS";
    }

    @Override
    public MeasureGraphView.GraphAdapter<?> getGraphAdapter() {
        return adapter;
    }

    @Override
    public long getTimeStarted() {
        return started;
    }

    @Override
    public long getDataUsed() {
        return 0;
    }

    @Override
    public String getName() {
        return "FPS speed";
    }

    @Override
    public CharSequence getResultData() {
        return String.format("%s frames rendered in total", Utils.shortNumber(view.getFramesTotal(), 1));
    }
}
