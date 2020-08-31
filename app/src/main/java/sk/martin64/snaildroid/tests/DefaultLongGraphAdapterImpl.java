package sk.martin64.snaildroid.tests;

import java.util.ArrayList;
import java.util.List;

import sk.martin64.snaildroid.view.MeasureGraphView;

public class DefaultLongGraphAdapterImpl extends MeasureGraphView.GraphAdapter<Long> {

    private boolean changed = false;
    private long max;
    private List<LongPoint> points = new ArrayList<>();

    @Override
    public synchronized void setMax(Long max) {
        this.max = max;
        this.changed = true;
        updateGraph();
    }

    @Override
    public synchronized Long getMax() {
        return max;
    }

    @Override
    protected synchronized boolean hasChanged() {
        return changed;
    }

    @Override
    protected synchronized void getPoints(List<MeasureGraphView.Point> target) {
        for (LongPoint p : points) {
            p.setY(max > 0 ? ((float) p.getLongY() / max) : 0);
        }

        target.addAll(points);
        changed = false;
    }

    @Override
    public synchronized void addPoint(int x, Long y) {
        if (y > max) {
            this.max = y;
        }
        this.points.add(new LongPoint(x, y));
        changed = true;
        updateGraph();
    }

    @Override
    public void addZeroPoint(int x) {
        addPoint(x, 0L);
    }

    private static class LongPoint extends MeasureGraphView.Point {
        private long y;
        public LongPoint(float x, long y) {
            super(x, y);
            this.y = y;
        }

        public long getLongY() {
            return y;
        }
    }
}
