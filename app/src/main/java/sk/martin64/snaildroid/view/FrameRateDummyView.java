package sk.martin64.snaildroid.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class FrameRateDummyView extends View {
    private boolean measure = false;
    private long mCounterTotal, mTime;

    public FrameRateDummyView(Context context) {
        super(context);
    }

    public FrameRateDummyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FrameRateDummyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FrameRateDummyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void startMeasure() {
        synchronized (this) {
            this.measure = true;
            this.mCounterTotal = 0;
            this.mTime = System.currentTimeMillis();
            invalidate();
        }
    }

    public void stopMeasure() {
        synchronized (this) {
            this.measure = false;
        }
    }

    public long getFramesTotal() {
        return mCounterTotal;
    }

    public long getStartTime() {
        return mTime;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(1, 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mCounterTotal++;

        synchronized (this) {
            if (measure) invalidate();
        }
    }
}