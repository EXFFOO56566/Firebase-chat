package com.bytesbee.firebase.chat.activities.views.audiowave;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;

import com.bytesbee.firebase.chat.activities.R;


public class Config {
    private int color, startColor, endColor;
    private float thickness;
    private Boolean colorGradient = false;
    private final AudioWave audioWave;

    private Paint PaintWave = new Paint();

    public Config(Context context, AttributeSet attrs, AudioWave audioWave) {
        this.audioWave = audioWave;
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AudioWave, 0, 0);
        if (attrs != null) {
            thickness = a.getFloat(R.styleable.AudioWave_waveThickness, 1f);
            color = a.getColor(R.styleable.AudioWave_waveColor, Color.parseColor("#691A40"));
            colorGradient = a.getBoolean(R.styleable.AudioWave_colorGradient, false);
            startColor = a.getColor(R.styleable.AudioWave_startColor, Color.parseColor("#93278F"));
            endColor = a.getColor(R.styleable.AudioWave_endColor, Color.parseColor("#00A99D"));
            a.recycle();
            PaintWave.setStrokeWidth(thickness);
            PaintWave.setAntiAlias(true);
            PaintWave.setStyle(Paint.Style.FILL);
            PaintWave.setColor(color);
            PaintWave.setAlpha(255);
        }
    }

    public int getColor() {
        return color;
    }

    public Config setColor(int color) {
        this.color = color;
        PaintWave.setColor(this.color);
        audioWave.invalidate();
        return this;
    }

    public int getStartColor() {
        return startColor;
    }

    public Config setStartColor(int startColor) {
        this.startColor = startColor;
        audioWave.invalidate();
        return this;
    }

    public int getEndColor() {
        return endColor;
    }

    public Config setEndColor(int endColor) {
        this.endColor = endColor;
        audioWave.invalidate();
        return this;
    }

    public float getThickness() {
        return thickness;
    }

    public Config setThickness(float thickness) {
        this.thickness = thickness;
        PaintWave.setStrokeWidth(this.thickness);
        audioWave.invalidate();
        return this;
    }

    public Boolean getColorGradient() {
        return colorGradient;
    }

    public Config setColorGradient(Boolean colorGradient) {
        this.colorGradient = colorGradient;
        audioWave.invalidate();
        return this;
    }

    public Paint getPaintWave() {
        return PaintWave;
    }

    public Config setPaintWave(Paint paintWave) {
        PaintWave = paintWave;
        audioWave.invalidate();
        return this;
    }

    public Paint setGradients(AudioWave audioWave) {
        PaintWave.setShader(new LinearGradient(0, 0,
                audioWave.getWidth(), 0,
                startColor, endColor, Shader.TileMode.MIRROR));
        audioWave.invalidate();
        return PaintWave;
    }

    public Paint reSetupPaint() {
        PaintWave = new Paint();
        PaintWave.setStrokeWidth(thickness);
        PaintWave.setAntiAlias(true);
        PaintWave.setStyle(Paint.Style.FILL);
        PaintWave.setColor(color);
        PaintWave.setAlpha(255);
        return PaintWave;
    }
}
