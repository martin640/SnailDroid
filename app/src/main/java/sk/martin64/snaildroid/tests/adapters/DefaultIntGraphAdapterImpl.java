package sk.martin64.snaildroid.tests.adapters;

import java.util.ArrayList;
import java.util.List;

import sk.martin64.snaildroid.view.MeasureGraphView;

public class DefaultIntGraphAdapterImpl extends MeasureGraphView.GraphAdapter<Integer> {

    private boolean changed = false;
    private int max;
    private List<IntPoint> points = new ArrayList<>();

    @Override
    public synchronized void setMax(Integer max) {
        this.max = max;
        this.changed = true;
        updateGraph();
    }

    @Override
    public synchronized Integer getMax() {
        return max;
    }

    @Override
    protected synchronized boolean hasChanged() {
        return changed;
    }

    @Override
    protected synchronized void getPoints(List<MeasureGraphView.Point> target) {
        for (IntPoint p : points) {
            p.setY(max > 0 ? ((float) p.getLongY() / max) : 0);
        }

        target.addAll(points);
        changed = false;
    }

    @Override
    public synchronized void addPoint(int x, Integer y) {
        if (y > max) {
            this.max = y;
        }
        this.points.add(new IntPoint(x, y));
        changed = true;
        updateGraph();
    }

    @Override
    public void addZeroPoint(int x) {
        addPoint(x, 0);
    }

    private static class IntPoint extends MeasureGraphView.Point {
        private long y;
        public IntPoint(float x, long y) {
            super(x, y);
            this.y = y;
        }

        public long getLongY() {
            return y;
        }
    }
}
