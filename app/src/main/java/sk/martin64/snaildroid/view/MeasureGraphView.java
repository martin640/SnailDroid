package sk.martin64.snaildroid.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MeasureGraphView extends View {

    private final Object adapterLock = new Object();
    private final List<Point> pointsCache = new ArrayList<>();
    private GraphAdapter<?> adapter;
    private Paint mainPaint, linePaint;
    private int graphColor, lineColor;
    private Shader shader;

    public MeasureGraphView(Context context) {
        this(context, null);
    }

    public MeasureGraphView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeasureGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MeasureGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        graphColor = 0xFFFF0000;
        lineColor = 0xFFC1C1C1;
    }

    public int getGraphColor() {
        return graphColor;
    }

    public void setGraphColor(int graphColor) {
        this.graphColor = graphColor;
        updateInternally();
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
        updateInternally();
    }

    public void setShader(Shader shader) {
        this.shader = shader;
        updateInternally();
    }

    public void setAdapter(GraphAdapter<?> adapter) {
        synchronized (adapterLock) {
            if (this.adapter != null) {
                this.adapter.graphView = null;
            }
            if (adapter.graphView != null) {
                throw new RuntimeException("Adapter is already attached to MeasureGraphView");
            }
            this.adapter = adapter;
            this.adapter.graphView = this;
        }
        updateInternally();
    }

    public GraphAdapter<?> getAdapter() {
        synchronized (adapterLock) {
            return adapter;
        }
    }

    private void updateInternally() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (adapterLock) {
            if (adapter == null) {
                Log.w("MGraphView", "onDraw(): No adapter set; skipping...");
                return;
            }

            if (mainPaint == null) {
                mainPaint = new Paint();
                mainPaint.setColor(graphColor);
                mainPaint.setStrokeWidth(2f);
                if (shader != null) {
                    mainPaint.setShader(shader);
                }

                linePaint = new Paint();
                linePaint.setColor(lineColor);
                linePaint.setStrokeWidth(2f);
            }

            if (adapter.hasChanged()) {
                pointsCache.clear();
                adapter.getPoints(pointsCache);
            }

            int height = getMeasuredHeight();

            float x = 0, y = 0;
            for (Point p : pointsCache) {
                p.yy = height - (height * p.y);
                canvas.drawLine(p.x, p.yy, p.x, height, mainPaint);
                x = p.x;
                y = p.yy;
            }
            canvas.drawLine(x, y, getMeasuredWidth(), y, linePaint);
        }
    }

    public static abstract class GraphAdapter<BaseType extends Number> {
        private MeasureGraphView graphView;

        public abstract void setMax(BaseType max);
        public abstract BaseType getMax();
        protected abstract boolean hasChanged();
        protected abstract void getPoints(List<Point> target);
        public abstract void addPoint(int x, BaseType y);
        public abstract void addZeroPoint(int x);

        public final void updateGraph() {
            if (graphView != null)
                graphView.updateInternally();
        }
    }

    public static class Point {
        private float x, y;
        /* internal variable used by graph */ float yy;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public synchronized void setY(float y) {
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }
}