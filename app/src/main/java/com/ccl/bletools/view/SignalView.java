package com.ccl.bletools.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;


/**
 * Author by Wang, Date on 2018/4/19.
 */
public class SignalView extends View {
    private int mSignal = -100;
    private int mStep = 0;
    private int mHight;
    private int mWidth;
    private Paint mPaint;

    public SignalView(Context context) {
        this(context, null);
    }

    public SignalView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setColor(Color.DKGRAY);
    }

    public int getSignal() {
        return mSignal;
    }

    public void setSignal(int signal) {
        if(signal < -100){
            signal = -100;
        }

        if(signal > 0){
            signal = 0;
        }
        if (signal != mSignal) {
            mSignal = signal;
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mStep = w / 100;
        mHight = h;
        mWidth = w;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int drawWidth = (100 - Math.abs(mSignal)) * mStep;
        canvas.drawRect(mWidth - drawWidth, 0, mWidth, mHight, mPaint);
    }
}
