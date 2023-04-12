package com.bytesbee.firebase.chat.activities.views.audiowave;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class AudioWave extends View {
    final int size = 4;
    private byte[] mBytes;
    private float[] mPoints;
    private final Rect mRect = new Rect();
    private Config config;

    public AudioWave(Context context) {
        super(context);
        init(context, null);
    }

    public AudioWave(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AudioWave(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mBytes = null;
        config = new Config(context, attrs, this);
    }

    public void updateVisualizer(byte[] bytes) {
        mBytes = bytes;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBytes == null) {
            return;
        }
        if (mPoints == null || mPoints.length < mBytes.length * size) {
            mPoints = new float[mBytes.length * size];
        }
        mRect.set(0, 0, getWidth(), getHeight());
        for (int i = 0; i < mBytes.length - 1; i++) {
            mPoints[i * size] = mRect.width() * i / (mBytes.length - 1);
            mPoints[i * size + 1] = mRect.height() / 2 + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;
            mPoints[i * size + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);
            mPoints[i * size + 3] = mRect.height() / 2 + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;
        }
        if (config.getColorGradient()) {
            config.reSetupPaint();
            config.setGradients(this);
        } else {
            config.reSetupPaint();
        }
        canvas.drawLines(mPoints, config.getPaintWave());
    }

    public Config getConfig() {
        return config;
    }

    public AudioWave setConfig(Config config) {
        this.config = config;
        return this;
    }
}
