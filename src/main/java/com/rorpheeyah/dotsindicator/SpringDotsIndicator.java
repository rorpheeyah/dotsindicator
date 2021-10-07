package com.rorpheeyah.dotsindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import java.util.ArrayList;
import java.util.List;

import static android.widget.LinearLayout.HORIZONTAL;

import static com.rorpheeyah.dotsindicator.UiUtils.getThemePrimaryColor;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

/*
     ____             _             ____        _       ___           _ _           _
    / ___| _ __  _ __(_)_ __   __ _|  _ \  ___ | |_ ___|_ _|_ __   __| (_) ___ __ _| |_ ___  _ __
    \___ \| '_ \| '__| | '_ \ / _` | | | |/ _ \| __/ __|| || '_ \ / _` | |/ __/ _` | __/ _ \| '__|
     ___) | |_) | |  | | | | | (_| | |_| | (_) | |_\__ \| || | | | (_| | | (_| (_| | || (_) | |
    |____/| .__/|_|  |_|_| |_|\__, |____/ \___/ \__|___/___|_| |_|\__,_|_|\___\__,_|\__\___/|_|
          |_|                 |___/
 */

/**
 * @author Matt Rorpheeyah
 */
public class SpringDotsIndicator extends FrameLayout {
    public static final float DEFAULT_DAMPING_RATIO = 0.5f;
    public static final int DEFAULT_STIFFNESS = 300;

    private final List<ImageView> strokeDots;
    private View dotIndicatorView;
    private ViewPager viewPager;
    private ViewPager2 viewPager2;

    // Attributes
    private int dotsStrokeSize;
    private int dotsSpacing;
    private int dotsStrokeWidth;
    private int dotsCornerRadius;
    private int dotsStrokeColor;
    private int dotIndicatorColor;
    private float stiffness;
    private float dampingRatio;

    private final int dotIndicatorSize;
    private final int dotIndicatorAdditionalSize;
    private final int horizontalMargin;
    private SpringAnimation dotIndicatorSpring;
    private final LinearLayout strokeDotsLinearLayout;

    private boolean dotsClickable, dotsFilled;
    private ViewPager.OnPageChangeListener pageChangedListener;
    private ViewPager2.OnPageChangeCallback pageChangedListener2;

    public SpringDotsIndicator(Context context) {
        this(context, null);
    }

    public SpringDotsIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpringDotsIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        strokeDots = new ArrayList<>();
        strokeDotsLinearLayout = new LinearLayout(context);
        LayoutParams linearParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        horizontalMargin = dpToPx(24);

        linearParams.setMargins(horizontalMargin, 0, horizontalMargin, 0);
        strokeDotsLinearLayout.setLayoutParams(linearParams);
        strokeDotsLinearLayout.setOrientation(HORIZONTAL);
        addView(strokeDotsLinearLayout);

        dotsStrokeSize              = dpToPx(16);
        dotsSpacing                 = dpToPx(4);
        dotsStrokeWidth             = dpToPx(2);
        dotIndicatorAdditionalSize  = dpToPx(1); // 1dp additional to fill the stroke dots
        dotsCornerRadius            = dotsStrokeSize / 2; // 1dp additional to fill the stroke dots
        dotIndicatorColor           = getThemePrimaryColor(context);
        dotsStrokeColor             = dotIndicatorColor;
        stiffness                   = DEFAULT_STIFFNESS;
        dampingRatio                = DEFAULT_DAMPING_RATIO;
        dotsClickable               = true;

        if (attrs != null) {
            TypedArray a        = getContext().obtainStyledAttributes(attrs, R.styleable.SpringDotsIndicator);

            // Dots attributes
            dotIndicatorColor   = a.getColor(R.styleable.SpringDotsIndicator_dotsColor, dotIndicatorColor);
            dotsStrokeColor     = a.getColor(R.styleable.SpringDotsIndicator_dotsStrokeColor, dotIndicatorColor);
            dotsStrokeSize      = (int) a.getDimension(R.styleable.SpringDotsIndicator_dotsSize, dotsStrokeSize);
            dotsSpacing         = (int) a.getDimension(R.styleable.SpringDotsIndicator_dotsSpacing, dotsSpacing);
            dotsCornerRadius    = (int) a.getDimension(R.styleable.SpringDotsIndicator_dotsCornerRadius, (float) dotsStrokeSize / 2);
            stiffness           = a.getFloat(R.styleable.SpringDotsIndicator_stiffness, stiffness);
            dampingRatio        = a.getFloat(R.styleable.SpringDotsIndicator_dampingRatio, dampingRatio);
            dotsFilled          = a.getBoolean(R.styleable.SpringDotsIndicator_dots_filled, false);

            // Spring dots attributes
            dotsStrokeWidth     = (int) a.getDimension(R.styleable.SpringDotsIndicator_dotsStrokeWidth, dotsStrokeWidth);

            a.recycle();
        }

        dotIndicatorSize        = dotsFilled ? dotsStrokeSize : dotsStrokeSize - dotsStrokeWidth * 2 + dotIndicatorAdditionalSize;

        if (isInEditMode()) {
            addStrokeDots(5);
            addView(buildDot(false));
        }
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        refreshDots();
        refreshDots2();
    }

    /**
     * Refresh dot for Viewpager
     */
    private void refreshDots() {
        if (dotIndicatorView == null) {
            setUpDotIndicator();
        }

        if (viewPager != null && viewPager.getAdapter() != null) {
            // Check if we need to refresh the strokeDots count
            if (strokeDots.size() < viewPager.getAdapter().getCount()) {
                addStrokeDots(viewPager.getAdapter().getCount() - strokeDots.size());
            } else if (strokeDots.size() > viewPager.getAdapter().getCount()) {
                removeDots(strokeDots.size() - viewPager.getAdapter().getCount());
            }
            setUpDotsAnimators();
        } else {
            Log.e(SpringDotsIndicator.class.getSimpleName(), "You have to set an adapter to the view pager before !");
        }
    }

    /**
     * Refresh dot for Viewpager2
     */
    private void refreshDots2() {
        if (dotIndicatorView == null) {
            setUpDotIndicator();
        }

        if (viewPager2 != null && viewPager2.getAdapter() != null) {
            // Check if we need to refresh the strokeDots count
            if (strokeDots.size() < viewPager2.getAdapter().getItemCount()) {
                addStrokeDots2(viewPager2.getAdapter().getItemCount() - strokeDots.size());
            } else if (strokeDots.size() > viewPager2.getAdapter().getItemCount()) {
                removeDots(strokeDots.size() - viewPager2.getAdapter().getItemCount());
            }
            setUpDotsAnimators2();
        } else {
            Log.e(SpringDotsIndicator.class.getSimpleName(), "You have to set an adapter to the view pager before !");
        }
    }

    private void setUpDotIndicator() {
        dotIndicatorView = buildDot(false);
        addView(dotIndicatorView);
        dotIndicatorSpring = new SpringAnimation(dotIndicatorView, SpringAnimation.TRANSLATION_X);
        SpringForce springForce = new SpringForce(0);
        springForce.setDampingRatio(dampingRatio);
        springForce.setStiffness(stiffness);
        dotIndicatorSpring.setSpring(springForce);
    }

    /**
     * Add Stroke for ViewPager
     */
    private void addStrokeDots(int count) {
        for (int i = 0; i < count; i++) {
            ViewGroup dot = buildDot(true);
            final int finalI = i;
            dot.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) {
                    if (dotsClickable && viewPager != null && viewPager.getAdapter() != null && finalI < viewPager.getAdapter().getCount()) {
                        viewPager.setCurrentItem(finalI, true);
                    }
                }
            });

            strokeDots.add((ImageView) dot.findViewById(R.id.spring_dot));
            strokeDotsLinearLayout.addView(dot);
        }
    }

    /**
     * Add Stroke for ViewPager2
     */
    private void addStrokeDots2(int count) {
        for (int i = 0; i < count; i++) {
            ViewGroup dot = buildDot(true);
            final int finalI = i;
            dot.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) {
                    if (dotsClickable && viewPager2 != null && viewPager2.getAdapter() != null && finalI < viewPager2.getAdapter().getItemCount()) {
                        viewPager2.setCurrentItem(finalI, true);
                    }
                }
            });

            strokeDots.add((ImageView) dot.findViewById(R.id.spring_dot));
            strokeDotsLinearLayout.addView(dot);
        }
    }

    private ViewGroup buildDot(boolean stroke) {
        ViewGroup dot = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.spring_dot_layout, this, false);
        ImageView dotView = dot.findViewById(R.id.spring_dot);
        dotView.setBackground(
                ContextCompat.getDrawable(getContext(), stroke ? R.drawable.spring_dot_stroke_background : R.drawable.spring_dot_background));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dotView.getLayoutParams();
        params.width = params.height = stroke ? dotsStrokeSize : dotIndicatorSize;
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        params.setMargins(dotsSpacing, 0, dotsSpacing, 0);

        setUpDotBackground(stroke, dotView);
        return dot;
    }

    private void setUpDotBackground(boolean stroke, @NonNull View dotView) {
        GradientDrawable dotBackground = (GradientDrawable) dotView.getBackground();
        if (stroke) {
            dotBackground.setStroke(dotsStrokeWidth, dotsStrokeColor);
            if(dotsFilled) dotBackground.setColor(dotsStrokeColor);
        } else {
            dotBackground.setColor(dotIndicatorColor);
        }
        dotBackground.setCornerRadius(dotsCornerRadius);
    }

    /**
     * Remove dot
     */
    private void removeDots(int count) {
        for (int i = 0; i < count; i++) {
            strokeDotsLinearLayout.removeViewAt(strokeDotsLinearLayout.getChildCount() - 1);
            strokeDots.remove(strokeDots.size() - 1);
        }
    }

    /**
     * Set Animation for Viewpager
     */
    private void setUpDotsAnimators() {
        if (viewPager != null && viewPager.getAdapter() != null && viewPager.getAdapter().getCount() > 0) {
            if (pageChangedListener != null) {
                viewPager.removeOnPageChangeListener(pageChangedListener);
            }
            setUpOnPageChangedListener();
            viewPager.addOnPageChangeListener(pageChangedListener);
        }
    }

    /**
     * Set Animation for Viewpager2
     */
    private void setUpDotsAnimators2() {
        if (viewPager2 != null && viewPager2.getAdapter() != null && viewPager2.getAdapter().getItemCount() > 0) {
            if (pageChangedListener2 != null) {
                viewPager2.unregisterOnPageChangeCallback(pageChangedListener2);
            }
            setUpOnPageChangedListener2();
            viewPager2.registerOnPageChangeCallback(pageChangedListener2);
        }
    }

    /**
     * Set Page change listener for Viewpager
     */
    private void setUpOnPageChangedListener() {
        pageChangedListener = new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                setCurrentDotSelected(position, positionOffset);
            }

            @Override public void onPageSelected(int position) { }

            @Override public void onPageScrollStateChanged(int state) { }
        };
    }

    /**
     * Set Page change listener for Viewpager2
     */
    private void setUpOnPageChangedListener2() {
        pageChangedListener2 = new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                setCurrentDotSelected(position, positionOffset);
            }
            @Override public void onPageSelected(int position) {}
            @Override public void onPageScrollStateChanged(int state) {}
        };
    }

    /**
     * Set current selected position
     */
    private void setCurrentDotSelected(int position, float positionOffset){
        float globalPositionOffsetPixels = position * (dotsStrokeSize + dotsSpacing * 2) + (dotsStrokeSize + dotsSpacing * 2) * positionOffset;
        float indicatorTranslationX = dotsFilled ? globalPositionOffsetPixels + horizontalMargin : globalPositionOffsetPixels + horizontalMargin + dotsStrokeWidth - (float) dotIndicatorAdditionalSize / 2;
        dotIndicatorSpring.getSpring().setFinalPosition(indicatorTranslationX);

        if (!dotIndicatorSpring.isRunning()) {
            dotIndicatorSpring.start();
        }
    }

    /**
     * Setup dot with ViewPager
     */
    private void setUpViewPager() {
        if (viewPager.getAdapter() != null) {
            viewPager.getAdapter().registerDataSetObserver(new DataSetObserver() {
                @Override public void onChanged() {
                    super.onChanged();
                    refreshDots();
                }
            });
        }
    }

    /**
     * Setup dot with ViewPager2
     */
    private void setUpViewPager2() {
        if (viewPager2.getAdapter() != null) {
            viewPager2.getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    refreshDots2();
                }
            });
        }
    }

    private int dpToPx(int dp) {
        return (int) (getContext().getResources().getDisplayMetrics().density * dp);
    }

    /**
     * Set the indicator dot color.
     *
     * @param color the color fo the indicator dot.
     */
    public void setDotIndicatorColor(int color) {
        if (dotIndicatorView != null) {
            dotIndicatorColor = color;
            setUpDotBackground(false, dotIndicatorView);
        }
    }

    /**
     * Set the stroke indicator dots color.
     *
     * @param color the color fo the stroke indicator dots.
     */
    public void setStrokeDotsIndicatorColor(int color) {
        if (strokeDots != null && !strokeDots.isEmpty()) {
            dotsStrokeColor = color;
            for (ImageView v : strokeDots) {
                setUpDotBackground(true, v);
            }
        }
    }

    /**
     * Determine if the stroke dots are clickable to go the a page directly.
     *
     * @param dotsClickable true if dots are clickable.
     */
    public void setDotsClickable(boolean dotsClickable) {
        this.dotsClickable = dotsClickable;
    }

    /**
     * Attach ViewPager
     */
    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        setUpViewPager();
        refreshDots();
    }

    /**
     * Attach ViewPager2
     */
    public void setViewPager2(ViewPager2 viewPager2) {
        this.viewPager2 = viewPager2;
        setUpViewPager2();
        refreshDots2();
    }
}
