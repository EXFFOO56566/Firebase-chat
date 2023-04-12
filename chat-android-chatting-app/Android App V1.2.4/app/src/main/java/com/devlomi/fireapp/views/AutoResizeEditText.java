package com.devlomi.fireapp.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.util.TypedValue;

import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

/*this AutoResize Features was created by @ViksaaSkool
https://github.com/ViksaaSkool/AutoFitEditText
i just copied and pasted it to make it extends EmojiconEditText to support emojis
 */
public class AutoResizeEditText extends EmojiconEditText {
    private static final int NO_LINE_LIMIT = -1;
    private final RectF _availableSpaceRect;
    private final SparseIntArray _textCachedSizes;
    private final AutoResizeEditText.SizeTester _sizeTester;
    private float _maxTextSize;
    private float _spacingMult;
    private float _spacingAdd;
    private Float _minTextSize;
    private int _widthLimit;
    private int _maxLines;
    private boolean _enableSizeCache;
    private boolean _initiallized;
    private TextPaint paint;

    public AutoResizeEditText(Context context) {
        this(context, (AttributeSet) null, 0);
    }

    public AutoResizeEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoResizeEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this._availableSpaceRect = new RectF();
        this._textCachedSizes = new SparseIntArray();
        this._spacingMult = 1.0F;
        this._spacingAdd = 0.0F;
        this._enableSizeCache = true;
        this._initiallized = false;
        this._minTextSize = TypedValue.applyDimension(2, 12.0F, this.getResources().getDisplayMetrics());
        this._maxTextSize = this.getTextSize();
        if (this._maxLines == 0) {
            this._maxLines = -1;
        }

        this._sizeTester = new AutoResizeEditText.SizeTester() {
            final RectF textRect = new RectF();

            @TargetApi(16)
            public int onTestSize(int suggestedSize, RectF availableSPace) {
                AutoResizeEditText.this.paint.setTextSize((float) suggestedSize);
                String text = AutoResizeEditText.this.getText().toString();
                boolean singleline = AutoResizeEditText.this.getMaxLines() == 1;
                if (singleline) {
                    this.textRect.bottom = AutoResizeEditText.this.paint.getFontSpacing();
                    this.textRect.right = AutoResizeEditText.this.paint.measureText(text);
                } else {
                    StaticLayout layout = new StaticLayout(text, AutoResizeEditText.this.paint, AutoResizeEditText.this._widthLimit, Layout.Alignment.ALIGN_NORMAL, AutoResizeEditText.this._spacingMult, AutoResizeEditText.this._spacingAdd, true);
                    if (AutoResizeEditText.this.getMaxLines() != -1 && layout.getLineCount() > AutoResizeEditText.this.getMaxLines()) {
                        return 1;
                    }

                    this.textRect.bottom = (float) layout.getHeight();
                    int maxWidth = -1;

                    for (int i = 0; i < layout.getLineCount(); ++i) {
                        if ((float) maxWidth < layout.getLineWidth(i)) {
                            maxWidth = (int) layout.getLineWidth(i);
                        }
                    }

                    this.textRect.right = (float) maxWidth;
                }

                this.textRect.offsetTo(0.0F, 0.0F);
                return availableSPace.contains(this.textRect) ? -1 : 1;
            }
        };
        this._initiallized = true;
    }

    public void setTypeface(Typeface tf) {
        if (this.paint == null) {
            this.paint = new TextPaint(this.getPaint());
        }

        this.paint.setTypeface(tf);
        super.setTypeface(tf);
    }

    public void setTextSize(float size) {
        this._maxTextSize = size;
        this._textCachedSizes.clear();
        this.adjustTextSize();
    }

    public void setMaxLines(int maxlines) {
        super.setMaxLines(maxlines);
        this._maxLines = maxlines;
        this.reAdjust();
    }

    public int getMaxLines() {
        return this._maxLines;
    }

    public void setSingleLine() {
        super.setSingleLine();
        this._maxLines = 1;
        this.reAdjust();
    }

    public void setSingleLine(boolean singleLine) {
        super.setSingleLine(singleLine);
        if (singleLine) {
            this._maxLines = 1;
        } else {
            this._maxLines = -1;
        }

        this.reAdjust();
    }

    public void setLines(int lines) {
        super.setLines(lines);
        this._maxLines = lines;
        this.reAdjust();
    }

    public void setTextSize(int unit, float size) {
        Context c = this.getContext();
        Resources r;
        if (c == null) {
            r = Resources.getSystem();
        } else {
            r = c.getResources();
        }

        this._maxTextSize = TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
        this._textCachedSizes.clear();
        this.adjustTextSize();
    }

    public void setLineSpacing(float add, float mult) {
        super.setLineSpacing(add, mult);
        this._spacingMult = mult;
        this._spacingAdd = add;
    }

    public void setMinTextSize(Float minTextSize) {
        this._minTextSize = minTextSize;
        this.reAdjust();
    }

    public Float get_minTextSize() {
        return this._minTextSize;
    }

    private void reAdjust() {
        this.adjustTextSize();
    }

    private void adjustTextSize() {
        if (this._initiallized) {
            int startSize = Math.round(this._minTextSize);
            int heightLimit = this.getMeasuredHeight() - this.getCompoundPaddingBottom() - this.getCompoundPaddingTop();
            this._widthLimit = this.getMeasuredWidth() - this.getCompoundPaddingLeft() - this.getCompoundPaddingRight();
            if (this._widthLimit > 0) {
                this._availableSpaceRect.right = (float) this._widthLimit;
                this._availableSpaceRect.bottom = (float) heightLimit;
                super.setTextSize(0, (float) this.efficientTextSizeSearch(startSize, (int) this._maxTextSize, this._sizeTester, this._availableSpaceRect));
            }
        }
    }

    public void setEnableSizeCache(boolean enable) {
        this._enableSizeCache = enable;
        this._textCachedSizes.clear();
        this.adjustTextSize();
    }

    private int efficientTextSizeSearch(int start, int end, AutoResizeEditText.SizeTester sizeTester, RectF availableSpace) {
        if (!this._enableSizeCache) {
            return this.binarySearch(start, end, sizeTester, availableSpace);
        } else {
            String text = this.getText().toString();
            int key = text == null ? 0 : text.length();
            int size = this._textCachedSizes.get(key);
            if (size != 0) {
                return size;
            } else {
                size = this.binarySearch(start, end, sizeTester, availableSpace);
                this._textCachedSizes.put(key, size);
                return size;
            }
        }
    }

    private int binarySearch(int start, int end, AutoResizeEditText.SizeTester sizeTester, RectF availableSpace) {
        int lastBest = start;
        int lo = start;
        int hi = end - 1;
        boolean var8 = false;

        while (lo <= hi) {
            int mid = lo + hi >>> 1;
            int midValCmp = sizeTester.onTestSize(mid, availableSpace);
            if (midValCmp < 0) {
                lastBest = lo;
                lo = mid + 1;
            } else {
                if (midValCmp <= 0) {
                    return mid;
                }

                hi = mid - 1;
                lastBest = hi;
            }
        }

        return lastBest;
    }

    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        super.onTextChanged(text, start, before, after);
        this.reAdjust();
    }

    protected void onSizeChanged(int width, int height, int oldwidth, int oldheight) {
        this._textCachedSizes.clear();
        super.onSizeChanged(width, height, oldwidth, oldheight);
        if (width != oldwidth || height != oldheight) {
            this.reAdjust();
        }

    }

    private interface SizeTester {
        int onTestSize(int var1, RectF var2);
    }

}
