package com.getbase.floatingactionbutton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.ShapeDrawable.ShaderFactory;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build.VERSION;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.TextView;
import eu.chainfire.flash.shell.perform.Constants;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class FloatingActionButton extends ImageButton {
    public static final int SIZE_MINI = 1;
    public static final int SIZE_NORMAL = 0;
    private float mCircleSize;
    int mColorDisabled;
    int mColorNormal;
    int mColorPressed;
    private int mDrawableSize;
    private int mIcon;
    private Drawable mIconDrawable;
    private float mShadowOffset;
    private float mShadowRadius;
    private int mSize;
    boolean mStrokeVisible;
    String mTitle;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({0, 1})
    public @interface FAB_SIZE {
    }

    private static class TranslucentLayerDrawable extends LayerDrawable {
        private final int mAlpha;

        public TranslucentLayerDrawable(int alpha, Drawable... layers) {
            super(layers);
            this.mAlpha = alpha;
        }

        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            canvas.saveLayerAlpha((float) bounds.left, (float) bounds.top, (float) bounds.right, (float) bounds.bottom, this.mAlpha, 31);
            super.draw(canvas);
            canvas.restore();
        }
    }

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    void init(Context context, AttributeSet attributeSet) {
        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionButton, 0, 0);
        this.mColorNormal = attr.getColor(R.styleable.FloatingActionButton_fab_colorNormal, getColor(17170451));
        this.mColorPressed = attr.getColor(R.styleable.FloatingActionButton_fab_colorPressed, getColor(17170450));
        this.mColorDisabled = attr.getColor(R.styleable.FloatingActionButton_fab_colorDisabled, getColor(17170432));
        this.mSize = attr.getInt(R.styleable.FloatingActionButton_fab_size, 0);
        this.mIcon = attr.getResourceId(R.styleable.FloatingActionButton_fab_icon, 0);
        this.mTitle = attr.getString(R.styleable.FloatingActionButton_fab_title);
        this.mStrokeVisible = attr.getBoolean(R.styleable.FloatingActionButton_fab_stroke_visible, true);
        attr.recycle();
        updateCircleSize();
        this.mShadowRadius = getDimension(R.dimen.fab_shadow_radius);
        this.mShadowOffset = getDimension(R.dimen.fab_shadow_offset);
        updateDrawableSize();
        updateBackground();
    }

    private void updateDrawableSize() {
        this.mDrawableSize = (int) (this.mCircleSize + (2.0f * this.mShadowRadius));
    }

    private void updateCircleSize() {
        this.mCircleSize = getDimension(this.mSize == 0 ? R.dimen.fab_size_normal : R.dimen.fab_size_mini);
    }

    public void setSize(int size) {
        if (size != SIZE_MINI && size != 0) {
            throw new IllegalArgumentException("Use @FAB_SIZE constants only!");
        } else if (this.mSize != size) {
            this.mSize = size;
            updateCircleSize();
            updateDrawableSize();
            updateBackground();
        }
    }

    public int getSize() {
        return this.mSize;
    }

    public void setIcon(int icon) {
        if (this.mIcon != icon) {
            this.mIcon = icon;
            this.mIconDrawable = null;
            updateBackground();
        }
    }

    public void setIconDrawable(@NonNull Drawable iconDrawable) {
        if (this.mIconDrawable != iconDrawable) {
            this.mIcon = 0;
            this.mIconDrawable = iconDrawable;
            updateBackground();
        }
    }

    public int getColorNormal() {
        return this.mColorNormal;
    }

    public void setColorNormalResId(int colorNormal) {
        setColorNormal(getColor(colorNormal));
    }

    public void setColorNormal(int color) {
        if (this.mColorNormal != color) {
            this.mColorNormal = color;
            updateBackground();
        }
    }

    public int getColorPressed() {
        return this.mColorPressed;
    }

    public void setColorPressedResId(int colorPressed) {
        setColorPressed(getColor(colorPressed));
    }

    public void setColorPressed(int color) {
        if (this.mColorPressed != color) {
            this.mColorPressed = color;
            updateBackground();
        }
    }

    public int getColorDisabled() {
        return this.mColorDisabled;
    }

    public void setColorDisabledResId(int colorDisabled) {
        setColorDisabled(getColor(colorDisabled));
    }

    public void setColorDisabled(int color) {
        if (this.mColorDisabled != color) {
            this.mColorDisabled = color;
            updateBackground();
        }
    }

    public void setStrokeVisible(boolean visible) {
        if (this.mStrokeVisible != visible) {
            this.mStrokeVisible = visible;
            updateBackground();
        }
    }

    public boolean isStrokeVisible() {
        return this.mStrokeVisible;
    }

    int getColor(int id) {
        return getResources().getColor(id);
    }

    float getDimension(int id) {
        return getResources().getDimension(id);
    }

    public void setTitle(String title) {
        this.mTitle = title;
        TextView label = getLabelView();
        if (label != null) {
            label.setText(title);
        }
    }

    TextView getLabelView() {
        return (TextView) getTag(R.id.fab_label);
    }

    public String getTitle() {
        return this.mTitle;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(this.mDrawableSize, this.mDrawableSize);
    }

    void updateBackground() {
        float strokeWidth = getDimension(R.dimen.fab_stroke_width);
        float halfStrokeWidth = strokeWidth / 2.0f;
        Drawable[] drawableArr = new Drawable[4];
        drawableArr[0] = getResources().getDrawable(this.mSize == 0 ? R.drawable.fab_bg_normal : R.drawable.fab_bg_mini);
        drawableArr[SIZE_MINI] = createFillDrawable(strokeWidth);
        drawableArr[2] = createOuterStrokeDrawable(strokeWidth);
        drawableArr[3] = getIconDrawable();
        LayerDrawable layerDrawable = new LayerDrawable(drawableArr);
        int iconOffset = ((int) (this.mCircleSize - getDimension(R.dimen.fab_icon_size))) / 2;
        int circleInsetHorizontal = (int) this.mShadowRadius;
        int circleInsetTop = (int) (this.mShadowRadius - this.mShadowOffset);
        int circleInsetBottom = (int) (this.mShadowRadius + this.mShadowOffset);
        layerDrawable.setLayerInset(SIZE_MINI, circleInsetHorizontal, circleInsetTop, circleInsetHorizontal, circleInsetBottom);
        layerDrawable.setLayerInset(2, (int) (((float) circleInsetHorizontal) - halfStrokeWidth), (int) (((float) circleInsetTop) - halfStrokeWidth), (int) (((float) circleInsetHorizontal) - halfStrokeWidth), (int) (((float) circleInsetBottom) - halfStrokeWidth));
        layerDrawable.setLayerInset(3, circleInsetHorizontal + iconOffset, circleInsetTop + iconOffset, circleInsetHorizontal + iconOffset, circleInsetBottom + iconOffset);
        setBackgroundCompat(layerDrawable);
    }

    Drawable getIconDrawable() {
        if (this.mIconDrawable != null) {
            return this.mIconDrawable;
        }
        if (this.mIcon != 0) {
            return getResources().getDrawable(this.mIcon);
        }
        return new ColorDrawable(0);
    }

    private StateListDrawable createFillDrawable(float strokeWidth) {
        StateListDrawable drawable = new StateListDrawable();
        int[] iArr = new int[SIZE_MINI];
        iArr[0] = -16842910;
        drawable.addState(iArr, createCircleDrawable(this.mColorDisabled, strokeWidth));
        iArr = new int[SIZE_MINI];
        iArr[0] = 16842919;
        drawable.addState(iArr, createCircleDrawable(this.mColorPressed, strokeWidth));
        drawable.addState(new int[0], createCircleDrawable(this.mColorNormal, strokeWidth));
        return drawable;
    }

    private Drawable createCircleDrawable(int color, float strokeWidth) {
        int alpha = Color.alpha(color);
        int opaqueColor = opaque(color);
        Paint paint = new ShapeDrawable(new OvalShape()).getPaint();
        paint.setAntiAlias(true);
        paint.setColor(opaqueColor);
        Drawable[] layers = new Drawable[]{fillDrawable, createInnerStrokesDrawable(opaqueColor, strokeWidth)};
        LayerDrawable drawable = (alpha == 255 || !this.mStrokeVisible) ? new LayerDrawable(layers) : new TranslucentLayerDrawable(alpha, layers);
        int halfStrokeWidth = (int) (strokeWidth / 2.0f);
        drawable.setLayerInset(SIZE_MINI, halfStrokeWidth, halfStrokeWidth, halfStrokeWidth, halfStrokeWidth);
        return drawable;
    }

    private Drawable createOuterStrokeDrawable(float strokeWidth) {
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        Paint paint = shapeDrawable.getPaint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Style.STROKE);
        paint.setColor(Constants.COLOR_BACKGROUND);
        paint.setAlpha(opacityToAlpha(0.02f));
        return shapeDrawable;
    }

    private int opacityToAlpha(float opacity) {
        return (int) (255.0f * opacity);
    }

    private int darkenColor(int argb) {
        return adjustColorBrightness(argb, 0.9f);
    }

    private int lightenColor(int argb) {
        return adjustColorBrightness(argb, 1.1f);
    }

    private int adjustColorBrightness(int argb, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(argb, hsv);
        hsv[2] = Math.min(hsv[2] * factor, 1.0f);
        return Color.HSVToColor(Color.alpha(argb), hsv);
    }

    private int halfTransparent(int argb) {
        return Color.argb(Color.alpha(argb) / 2, Color.red(argb), Color.green(argb), Color.blue(argb));
    }

    private int opaque(int argb) {
        return Color.rgb(Color.red(argb), Color.green(argb), Color.blue(argb));
    }

    private Drawable createInnerStrokesDrawable(int color, float strokeWidth) {
        if (!this.mStrokeVisible) {
            return new ColorDrawable(0);
        }
        Drawable shapeDrawable = new ShapeDrawable(new OvalShape());
        final int bottomStrokeColor = darkenColor(color);
        final int bottomStrokeColorHalfTransparent = halfTransparent(bottomStrokeColor);
        final int topStrokeColor = lightenColor(color);
        final int topStrokeColorHalfTransparent = halfTransparent(topStrokeColor);
        Paint paint = shapeDrawable.getPaint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Style.STROKE);
        final int i = color;
        shapeDrawable.setShaderFactory(new ShaderFactory() {
            public Shader resize(int width, int height) {
                return new LinearGradient((float) (width / 2), 0.0f, (float) (width / 2), (float) height, new int[]{topStrokeColor, topStrokeColorHalfTransparent, i, bottomStrokeColorHalfTransparent, bottomStrokeColor}, new float[]{0.0f, 0.2f, 0.5f, 0.8f, 1.0f}, TileMode.CLAMP);
            }
        });
        return shapeDrawable;
    }

    @SuppressLint({"NewApi"})
    private void setBackgroundCompat(Drawable drawable) {
        if (VERSION.SDK_INT >= 16) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    public void setVisibility(int visibility) {
        TextView label = getLabelView();
        if (label != null) {
            label.setVisibility(visibility);
        }
        super.setVisibility(visibility);
    }
}
