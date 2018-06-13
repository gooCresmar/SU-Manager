package com.getbase.floatingactionbutton;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import me.zhanghai.android.materialprogressbar.R;

public class FloatingActionsMenu extends ViewGroup {
    private static final int ANIMATION_DURATION = 300;
    private static final float COLLAPSED_PLUS_ROTATION = 0.0f;
    private static final float EXPANDED_PLUS_ROTATION = 135.0f;
    public static final int EXPAND_DOWN = 1;
    public static final int EXPAND_LEFT = 2;
    public static final int EXPAND_RIGHT = 3;
    public static final int EXPAND_UP = 0;
    private static Interpolator sAlphaExpandInterpolator = new DecelerateInterpolator();
    private static Interpolator sCollapseInterpolator = new DecelerateInterpolator(3.0f);
    private static Interpolator sExpandInterpolator = new OvershootInterpolator();
    private AddFloatingActionButton mAddButton;
    private int mAddButtonColorNormal;
    private int mAddButtonColorPressed;
    private int mAddButtonPlusColor;
    private int mAddButtonSize;
    private boolean mAddButtonStrokeVisible;
    private int mButtonSpacing;
    private int mButtonsCount;
    private AnimatorSet mCollapseAnimation;
    private AnimatorSet mExpandAnimation;
    private int mExpandDirection;
    private boolean mExpanded;
    private int mLabelsMargin;
    private int mLabelsStyle;
    private int mLabelsVerticalOffset;
    private OnFloatingActionsMenuUpdateListener mListener;
    private int mMaxButtonHeight;
    private int mMaxButtonWidth;
    private RotatingDrawable mRotatingDrawable;

    private class LayoutParams extends android.view.ViewGroup.LayoutParams {
        private boolean animationsSetToPlay;
        private ObjectAnimator mCollapseAlpha = new ObjectAnimator();
        private ObjectAnimator mCollapseDir = new ObjectAnimator();
        private ObjectAnimator mExpandAlpha = new ObjectAnimator();
        private ObjectAnimator mExpandDir = new ObjectAnimator();

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
            this.mExpandDir.setInterpolator(FloatingActionsMenu.sExpandInterpolator);
            this.mExpandAlpha.setInterpolator(FloatingActionsMenu.sAlphaExpandInterpolator);
            this.mCollapseDir.setInterpolator(FloatingActionsMenu.sCollapseInterpolator);
            this.mCollapseAlpha.setInterpolator(FloatingActionsMenu.sCollapseInterpolator);
            this.mCollapseAlpha.setProperty(View.ALPHA);
            this.mCollapseAlpha.setFloatValues(new float[]{1.0f, FloatingActionsMenu.COLLAPSED_PLUS_ROTATION});
            this.mExpandAlpha.setProperty(View.ALPHA);
            this.mExpandAlpha.setFloatValues(new float[]{FloatingActionsMenu.COLLAPSED_PLUS_ROTATION, 1.0f});
            switch (FloatingActionsMenu.this.mExpandDirection) {
                case R.styleable.View_android_theme /*0*/:
                case FloatingActionsMenu.EXPAND_DOWN /*1*/:
                    this.mCollapseDir.setProperty(View.TRANSLATION_Y);
                    this.mExpandDir.setProperty(View.TRANSLATION_Y);
                    return;
                case FloatingActionsMenu.EXPAND_LEFT /*2*/:
                case FloatingActionsMenu.EXPAND_RIGHT /*3*/:
                    this.mCollapseDir.setProperty(View.TRANSLATION_X);
                    this.mExpandDir.setProperty(View.TRANSLATION_X);
                    return;
                default:
                    return;
            }
        }

        public void setAnimationsTarget(View view) {
            this.mCollapseAlpha.setTarget(view);
            this.mCollapseDir.setTarget(view);
            this.mExpandAlpha.setTarget(view);
            this.mExpandDir.setTarget(view);
            if (!this.animationsSetToPlay) {
                FloatingActionsMenu.this.mCollapseAnimation.play(this.mCollapseAlpha);
                FloatingActionsMenu.this.mCollapseAnimation.play(this.mCollapseDir);
                FloatingActionsMenu.this.mExpandAnimation.play(this.mExpandAlpha);
                FloatingActionsMenu.this.mExpandAnimation.play(this.mExpandDir);
                this.animationsSetToPlay = true;
            }
        }
    }

    public interface OnFloatingActionsMenuUpdateListener {
        void onMenuCollapsed();

        void onMenuExpanded();
    }

    private static class RotatingDrawable extends LayerDrawable {
        private float mRotation;

        public RotatingDrawable(Drawable drawable) {
            Drawable[] drawableArr = new Drawable[FloatingActionsMenu.EXPAND_DOWN];
            drawableArr[0] = drawable;
            super(drawableArr);
        }

        public float getRotation() {
            return this.mRotation;
        }

        public void setRotation(float rotation) {
            this.mRotation = rotation;
            invalidateSelf();
        }

        public void draw(Canvas canvas) {
            canvas.save();
            canvas.rotate(this.mRotation, (float) getBounds().centerX(), (float) getBounds().centerY());
            super.draw(canvas);
            canvas.restore();
        }
    }

    public static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public boolean mExpanded;

        public SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            boolean z = true;
            super(in);
            if (in.readInt() != FloatingActionsMenu.EXPAND_DOWN) {
                z = false;
            }
            this.mExpanded = z;
        }

        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.mExpanded ? FloatingActionsMenu.EXPAND_DOWN : 0);
        }
    }

    public FloatingActionsMenu(Context context) {
        this(context, null);
    }

    public FloatingActionsMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mExpandAnimation = new AnimatorSet().setDuration(300);
        this.mCollapseAnimation = new AnimatorSet().setDuration(300);
        init(context, attrs);
    }

    public FloatingActionsMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mExpandAnimation = new AnimatorSet().setDuration(300);
        this.mCollapseAnimation = new AnimatorSet().setDuration(300);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        this.mButtonSpacing = (int) ((getResources().getDimension(R.dimen.fab_actions_spacing) - getResources().getDimension(R.dimen.fab_shadow_radius)) - getResources().getDimension(R.dimen.fab_shadow_offset));
        this.mLabelsMargin = getResources().getDimensionPixelSize(R.dimen.fab_labels_margin);
        this.mLabelsVerticalOffset = getResources().getDimensionPixelSize(R.dimen.fab_shadow_offset);
        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionsMenu, 0, 0);
        this.mAddButtonPlusColor = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonPlusIconColor, getColor(17170443));
        this.mAddButtonColorNormal = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorNormal, getColor(17170451));
        this.mAddButtonColorPressed = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorPressed, getColor(17170450));
        this.mAddButtonSize = attr.getInt(R.styleable.FloatingActionsMenu_fab_addButtonSize, 0);
        this.mAddButtonStrokeVisible = attr.getBoolean(R.styleable.FloatingActionsMenu_fab_addButtonStrokeVisible, true);
        this.mExpandDirection = attr.getInt(R.styleable.FloatingActionsMenu_fab_expandDirection, 0);
        this.mLabelsStyle = attr.getResourceId(R.styleable.FloatingActionsMenu_fab_labelStyle, 0);
        attr.recycle();
        if (this.mLabelsStyle == 0 || !expandsHorizontally()) {
            createAddButton(context);
            return;
        }
        throw new IllegalStateException("Action labels in horizontal expand orientation is not supported.");
    }

    public void setOnFloatingActionsMenuUpdateListener(OnFloatingActionsMenuUpdateListener listener) {
        this.mListener = listener;
    }

    private boolean expandsHorizontally() {
        return this.mExpandDirection == EXPAND_LEFT || this.mExpandDirection == EXPAND_RIGHT;
    }

    private void createAddButton(Context context) {
        this.mAddButton = new AddFloatingActionButton(context) {
            void updateBackground() {
                this.mPlusColor = FloatingActionsMenu.this.mAddButtonPlusColor;
                this.mColorNormal = FloatingActionsMenu.this.mAddButtonColorNormal;
                this.mColorPressed = FloatingActionsMenu.this.mAddButtonColorPressed;
                this.mStrokeVisible = FloatingActionsMenu.this.mAddButtonStrokeVisible;
                super.updateBackground();
            }

            Drawable getIconDrawable() {
                RotatingDrawable rotatingDrawable = new RotatingDrawable(super.getIconDrawable());
                FloatingActionsMenu.this.mRotatingDrawable = rotatingDrawable;
                OvershootInterpolator interpolator = new OvershootInterpolator();
                ObjectAnimator collapseAnimator = ObjectAnimator.ofFloat(rotatingDrawable, "rotation", new float[]{FloatingActionsMenu.EXPANDED_PLUS_ROTATION, FloatingActionsMenu.COLLAPSED_PLUS_ROTATION});
                ObjectAnimator expandAnimator = ObjectAnimator.ofFloat(rotatingDrawable, "rotation", new float[]{FloatingActionsMenu.COLLAPSED_PLUS_ROTATION, FloatingActionsMenu.EXPANDED_PLUS_ROTATION});
                collapseAnimator.setInterpolator(interpolator);
                expandAnimator.setInterpolator(interpolator);
                FloatingActionsMenu.this.mExpandAnimation.play(expandAnimator);
                FloatingActionsMenu.this.mCollapseAnimation.play(collapseAnimator);
                return rotatingDrawable;
            }
        };
        this.mAddButton.setId(R.id.fab_expand_menu_button);
        this.mAddButton.setSize(this.mAddButtonSize);
        this.mAddButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FloatingActionsMenu.this.toggle();
            }
        });
        addView(this.mAddButton, super.generateDefaultLayoutParams());
    }

    public void addButton(FloatingActionButton button) {
        addView(button, this.mButtonsCount - 1);
        this.mButtonsCount += EXPAND_DOWN;
        if (this.mLabelsStyle != 0) {
            createLabels();
        }
    }

    public void removeButton(FloatingActionButton button) {
        removeView(button.getLabelView());
        removeView(button);
        this.mButtonsCount--;
    }

    private int getColor(int id) {
        return getResources().getColor(id);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i = 0;
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int width = 0;
        int height = 0;
        this.mMaxButtonWidth = 0;
        this.mMaxButtonHeight = 0;
        int maxLabelWidth = 0;
        for (int i2 = 0; i2 < this.mButtonsCount; i2 += EXPAND_DOWN) {
            View child = getChildAt(i2);
            if (child.getVisibility() != 8) {
                switch (this.mExpandDirection) {
                    case R.styleable.View_android_theme /*0*/:
                    case EXPAND_DOWN /*1*/:
                        this.mMaxButtonWidth = Math.max(this.mMaxButtonWidth, child.getMeasuredWidth());
                        height += child.getMeasuredHeight();
                        break;
                    case EXPAND_LEFT /*2*/:
                    case EXPAND_RIGHT /*3*/:
                        width += child.getMeasuredWidth();
                        this.mMaxButtonHeight = Math.max(this.mMaxButtonHeight, child.getMeasuredHeight());
                        break;
                }
                if (!expandsHorizontally()) {
                    TextView label = (TextView) child.getTag(R.id.fab_label);
                    if (label != null) {
                        maxLabelWidth = Math.max(maxLabelWidth, label.getMeasuredWidth());
                    }
                }
            }
        }
        if (expandsHorizontally()) {
            height = this.mMaxButtonHeight;
        } else {
            int i3 = this.mMaxButtonWidth;
            if (maxLabelWidth > 0) {
                i = this.mLabelsMargin + maxLabelWidth;
            }
            width = i3 + i;
        }
        switch (this.mExpandDirection) {
            case R.styleable.View_android_theme /*0*/:
            case EXPAND_DOWN /*1*/:
                height = adjustForOvershoot(height + (this.mButtonSpacing * (getChildCount() - 1)));
                break;
            case EXPAND_LEFT /*2*/:
            case EXPAND_RIGHT /*3*/:
                width = adjustForOvershoot(width + (this.mButtonSpacing * (getChildCount() - 1)));
                break;
        }
        setMeasuredDimension(width, height);
    }

    private int adjustForOvershoot(int dimension) {
        return (dimension * 12) / 10;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int i;
        View child;
        int childX;
        int childY;
        float collapsedTranslation;
        LayoutParams params;
        ObjectAnimator access$700;
        float[] fArr;
        switch (this.mExpandDirection) {
            case R.styleable.View_android_theme /*0*/:
            case EXPAND_DOWN /*1*/:
                boolean expandUp = this.mExpandDirection == 0;
                int addButtonY = expandUp ? (b - t) - this.mAddButton.getMeasuredHeight() : 0;
                int addButtonLeft = ((r - l) - this.mMaxButtonWidth) + ((this.mMaxButtonWidth - this.mAddButton.getMeasuredWidth()) / EXPAND_LEFT);
                this.mAddButton.layout(addButtonLeft, addButtonY, this.mAddButton.getMeasuredWidth() + addButtonLeft, this.mAddButton.getMeasuredHeight() + addButtonY);
                int labelsRight = ((r - l) - this.mMaxButtonWidth) - this.mLabelsMargin;
                int nextY = expandUp ? addButtonY - this.mButtonSpacing : (this.mAddButton.getMeasuredHeight() + addButtonY) + this.mButtonSpacing;
                for (i = this.mButtonsCount - 1; i >= 0; i--) {
                    child = getChildAt(i);
                    if (!(child == this.mAddButton || child.getVisibility() == 8)) {
                        childX = addButtonLeft + ((this.mAddButton.getMeasuredWidth() - child.getMeasuredWidth()) / EXPAND_LEFT);
                        if (expandUp) {
                            childY = nextY - child.getMeasuredHeight();
                        } else {
                            childY = nextY;
                        }
                        child.layout(childX, childY, child.getMeasuredWidth() + childX, child.getMeasuredHeight() + childY);
                        collapsedTranslation = (float) (addButtonY - childY);
                        child.setTranslationY(this.mExpanded ? COLLAPSED_PLUS_ROTATION : collapsedTranslation);
                        child.setAlpha(this.mExpanded ? 1.0f : COLLAPSED_PLUS_ROTATION);
                        params = (LayoutParams) child.getLayoutParams();
                        access$700 = params.mCollapseDir;
                        fArr = new float[EXPAND_LEFT];
                        fArr[0] = COLLAPSED_PLUS_ROTATION;
                        fArr[EXPAND_DOWN] = collapsedTranslation;
                        access$700.setFloatValues(fArr);
                        access$700 = params.mExpandDir;
                        fArr = new float[EXPAND_LEFT];
                        fArr[0] = collapsedTranslation;
                        fArr[EXPAND_DOWN] = COLLAPSED_PLUS_ROTATION;
                        access$700.setFloatValues(fArr);
                        params.setAnimationsTarget(child);
                        View label = (View) child.getTag(R.id.fab_label);
                        if (label != null) {
                            int labelTop = (childY - this.mLabelsVerticalOffset) + ((child.getMeasuredHeight() - label.getMeasuredHeight()) / EXPAND_LEFT);
                            label.layout(labelsRight - label.getMeasuredWidth(), labelTop, labelsRight, label.getMeasuredHeight() + labelTop);
                            label.setTranslationY(this.mExpanded ? COLLAPSED_PLUS_ROTATION : collapsedTranslation);
                            label.setAlpha(this.mExpanded ? 1.0f : COLLAPSED_PLUS_ROTATION);
                            LayoutParams labelParams = (LayoutParams) label.getLayoutParams();
                            access$700 = labelParams.mCollapseDir;
                            fArr = new float[EXPAND_LEFT];
                            fArr[0] = COLLAPSED_PLUS_ROTATION;
                            fArr[EXPAND_DOWN] = collapsedTranslation;
                            access$700.setFloatValues(fArr);
                            access$700 = labelParams.mExpandDir;
                            fArr = new float[EXPAND_LEFT];
                            fArr[0] = collapsedTranslation;
                            fArr[EXPAND_DOWN] = COLLAPSED_PLUS_ROTATION;
                            access$700.setFloatValues(fArr);
                            labelParams.setAnimationsTarget(label);
                        }
                        nextY = expandUp ? childY - this.mButtonSpacing : (child.getMeasuredHeight() + childY) + this.mButtonSpacing;
                    }
                }
                return;
            case EXPAND_LEFT /*2*/:
            case EXPAND_RIGHT /*3*/:
                boolean expandLeft = this.mExpandDirection == EXPAND_LEFT;
                int addButtonX = expandLeft ? (r - l) - this.mAddButton.getMeasuredWidth() : 0;
                int addButtonTop = ((b - t) - this.mMaxButtonHeight) + ((this.mMaxButtonHeight - this.mAddButton.getMeasuredHeight()) / EXPAND_LEFT);
                this.mAddButton.layout(addButtonX, addButtonTop, this.mAddButton.getMeasuredWidth() + addButtonX, this.mAddButton.getMeasuredHeight() + addButtonTop);
                int nextX = expandLeft ? addButtonX - this.mButtonSpacing : (this.mAddButton.getMeasuredWidth() + addButtonX) + this.mButtonSpacing;
                for (i = this.mButtonsCount - 1; i >= 0; i--) {
                    child = getChildAt(i);
                    if (!(child == this.mAddButton || child.getVisibility() == 8)) {
                        if (expandLeft) {
                            childX = nextX - child.getMeasuredWidth();
                        } else {
                            childX = nextX;
                        }
                        childY = addButtonTop + ((this.mAddButton.getMeasuredHeight() - child.getMeasuredHeight()) / EXPAND_LEFT);
                        child.layout(childX, childY, child.getMeasuredWidth() + childX, child.getMeasuredHeight() + childY);
                        collapsedTranslation = (float) (addButtonX - childX);
                        child.setTranslationX(this.mExpanded ? COLLAPSED_PLUS_ROTATION : collapsedTranslation);
                        child.setAlpha(this.mExpanded ? 1.0f : COLLAPSED_PLUS_ROTATION);
                        params = (LayoutParams) child.getLayoutParams();
                        access$700 = params.mCollapseDir;
                        fArr = new float[EXPAND_LEFT];
                        fArr[0] = COLLAPSED_PLUS_ROTATION;
                        fArr[EXPAND_DOWN] = collapsedTranslation;
                        access$700.setFloatValues(fArr);
                        access$700 = params.mExpandDir;
                        fArr = new float[EXPAND_LEFT];
                        fArr[0] = collapsedTranslation;
                        fArr[EXPAND_DOWN] = COLLAPSED_PLUS_ROTATION;
                        access$700.setFloatValues(fArr);
                        params.setAnimationsTarget(child);
                        if (expandLeft) {
                            nextX = childX - this.mButtonSpacing;
                        } else {
                            nextX = (child.getMeasuredWidth() + childX) + this.mButtonSpacing;
                        }
                    }
                }
                return;
            default:
                return;
        }
    }

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(super.generateDefaultLayoutParams());
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(super.generateLayoutParams(attrs));
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new LayoutParams(super.generateLayoutParams(p));
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        bringChildToFront(this.mAddButton);
        this.mButtonsCount = getChildCount();
        if (this.mLabelsStyle != 0) {
            createLabels();
        }
    }

    private void createLabels() {
        Context context = new ContextThemeWrapper(getContext(), this.mLabelsStyle);
        for (int i = 0; i < this.mButtonsCount; i += EXPAND_DOWN) {
            FloatingActionButton button = (FloatingActionButton) getChildAt(i);
            String title = button.getTitle();
            if (!(button == this.mAddButton || title == null || button.getTag(R.id.fab_label) != null)) {
                TextView label = new TextView(context);
                label.setText(button.getTitle());
                addView(label);
                button.setTag(R.id.fab_label, label);
            }
        }
    }

    public void collapse() {
        if (this.mExpanded) {
            this.mExpanded = false;
            this.mCollapseAnimation.start();
            this.mExpandAnimation.cancel();
            if (this.mListener != null) {
                this.mListener.onMenuCollapsed();
            }
        }
    }

    public void toggle() {
        if (this.mExpanded) {
            collapse();
        } else {
            expand();
        }
    }

    public void expand() {
        if (!this.mExpanded) {
            this.mExpanded = true;
            this.mCollapseAnimation.cancel();
            this.mExpandAnimation.start();
            if (this.mListener != null) {
                this.mListener.onMenuExpanded();
            }
        }
    }

    public boolean isExpanded() {
        return this.mExpanded;
    }

    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.mExpanded = this.mExpanded;
        return savedState;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            this.mExpanded = savedState.mExpanded;
            if (this.mRotatingDrawable != null) {
                this.mRotatingDrawable.setRotation(this.mExpanded ? EXPANDED_PLUS_ROTATION : COLLAPSED_PLUS_ROTATION);
            }
            super.onRestoreInstanceState(savedState.getSuperState());
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
