package com.rorpheeyah.dotsindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

/*
     ____        _       ___           _ _           _
    |  _ \  ___ | |_ ___|_ _|_ __   __| (_) ___ __ _| |_ ___  _ __
    | | | |/ _ \| __/ __|| || '_ \ / _` | |/ __/ _` | __/ _ \| '__|
    | |_| | (_) | |_\__ \| || | | | (_| | | (_| (_| | || (_) | |
    |____/ \___/ \__|___/___|_| |_|\__,_|_|\___\__,_|\__\___/|_|
 */

/**
 * @author Matt Rorpheeyah
 */
public class DotsIndicator extends LinearLayout {
    private static final int DEFAULT_POINT_COLOR = Color.WHITE;
    public static final float DEFAULT_WIDTH_FACTOR = 2.5f;

    private List<ImageView> dots;
    private ViewPager viewPager;
    private ViewPager2 viewPager2;
    private float dotsSize;
    private float dotsCornerRadius;
    private float dotsSpacing;
    private int currentPage;
    private float dotsWidthFactor;
    private int dotsColor;
    private int selectedDotColor;

    private boolean dotsClickable, isAllDot;
    private ViewPager.OnPageChangeListener pageChangedListener;
    private ViewPager2.OnPageChangeCallback pageChangedListener2;

    public DotsIndicator(Context context) {
        super(context);
        init(null);
    }

    public DotsIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DotsIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     *  Initiate views & attributes
     */
    private void init(AttributeSet attrs) {
        dots = new ArrayList<>();
        setOrientation(HORIZONTAL);

        dotsSize = dpToPx(16);
        dotsSpacing = dpToPx(4);
        dotsCornerRadius = dotsSize / 2;

        dotsWidthFactor = DEFAULT_WIDTH_FACTOR;
        dotsColor = DEFAULT_POINT_COLOR;
        dotsClickable = true;

        if (attrs != null) {
            TypedArray a        = getContext().obtainStyledAttributes(attrs, R.styleable.DotsIndicator);
            selectedDotColor    = a.getColor(R.styleable.DotsIndicator_selectedDotColor, DEFAULT_POINT_COLOR);
            dotsColor           = a.getColor(R.styleable.DotsIndicator_dotsColor, DEFAULT_POINT_COLOR);
            setUpCircleColors(dotsColor);

            dotsWidthFactor     = a.getFloat(R.styleable.DotsIndicator_dotsWidthFactor, 2.5f);
            if (dotsWidthFactor < 1) {
                dotsWidthFactor = 2.5f;
            }

            dotsSize            = a.getDimension(R.styleable.DotsIndicator_dotsSize, dotsSize);
            dotsCornerRadius    = (int) a.getDimension(R.styleable.DotsIndicator_dotsCornerRadius, dotsSize / 2);
            dotsSpacing         = a.getDimension(R.styleable.DotsIndicator_dotsSpacing, dotsSpacing);
            isAllDot            = a.getBoolean(R.styleable.DotsIndicator_dots_all, false);

            a.recycle();
        } else {
            setUpCircleColors(DEFAULT_POINT_COLOR);
        }

        if (isInEditMode()) {
            addDots(5);
            setUpSelectedColors(0);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        refreshDots();
        refreshDots2();
    }

    /**
     * Refresh dot for Viewpager
     */
    private void refreshDots() {
        if (viewPager != null && viewPager.getAdapter() != null) {
            // Check if we need to refresh the dots count
            if (dots.size() < viewPager.getAdapter().getCount()) {
                addDots(viewPager.getAdapter().getCount() - dots.size());
            } else if (dots.size() > viewPager.getAdapter().getCount()) {
                removeDots(dots.size() - viewPager.getAdapter().getCount());
            }
            setUpSelectedColors(currentPage);
            setUpDotsAnimators();
        } else {
            Log.e(DotsIndicator.class.getSimpleName(), "You have to set an adapter to the view pager before !");
        }
    }

    /**
     * Refresh dot for Viewpager2
     */
    private void refreshDots2() {
        if (viewPager2 != null && viewPager2.getAdapter() != null) {
            // Check if we need to refresh the dots count
            if (dots.size() < viewPager2.getAdapter().getItemCount()) {
                addDots2(viewPager2.getAdapter().getItemCount() - dots.size());
            } else if (dots.size() > viewPager2.getAdapter().getItemCount()) {
                removeDots(dots.size() - viewPager2.getAdapter().getItemCount());
            }
            setUpSelectedColors(currentPage);
            setUpDotsAnimators2();
        } else {
            Log.e(DotsIndicator.class.getSimpleName(), "You have to set an adapter to the view pager before !");
        }
    }

    /**
     * Add dot for Viewpager
     */
    private void addDots(int count) {
        for (int i = 0; i < count; i++) {
            View dot = LayoutInflater.from(getContext()).inflate(R.layout.dot_layout, this, false);
            ImageView imageView = dot.findViewById(R.id.dot);
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) imageView.getLayoutParams();
            params.width = params.height = (int) dotsSize;
            params.setMargins((int) dotsSpacing, 0, (int) dotsSpacing, 0);
            ((GradientDrawable) imageView.getBackground()).setCornerRadius(dotsCornerRadius);
            ((GradientDrawable) imageView.getBackground()).setColor(dotsColor);

            final int finalI = i;
            dot.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) {
                    if (dotsClickable
                            && viewPager != null
                            && viewPager.getAdapter() != null
                            && finalI < viewPager.getAdapter().getCount()) {
                        viewPager.setCurrentItem(finalI, true);
                    }
                }
            });

            dots.add(imageView);
            addView(dot);
        }
    }

    /**
     * Add dot for Viewpager2
     */
    private void addDots2(int count) {
        for (int i = 0; i < count; i++) {
            View dot = LayoutInflater.from(getContext()).inflate(R.layout.dot_layout, this, false);
            ImageView imageView = dot.findViewById(R.id.dot);
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) imageView.getLayoutParams();
            params.width = params.height = (int) dotsSize;
            params.setMargins((int) dotsSpacing, 0, (int) dotsSpacing, 0);
            ((GradientDrawable) imageView.getBackground()).setCornerRadius(dotsCornerRadius);
            ((GradientDrawable) imageView.getBackground()).setColor(dotsColor);

            final int finalI = i;
            dot.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) {
                    if (dotsClickable
                            && viewPager2 != null
                            && viewPager2.getAdapter() != null
                            && finalI < viewPager2.getAdapter().getItemCount()) {
                        viewPager2.setCurrentItem(finalI, true);
                    }
                }
            });

            dots.add(imageView);
            addView(dot);
        }
    }

    /**
     * Remove dot
     */
    private void removeDots(int count) {
        for (int i = 0; i < count; i++) {
            removeViewAt(getChildCount() - 1);
            dots.remove(dots.size() - 1);
        }
    }

    /**
     * Set Animation for Viewpager2
     */
    private void setUpDotsAnimators() {
        if (viewPager != null
                && viewPager.getAdapter() != null
                && viewPager.getAdapter().getCount() > 0) {
            if (currentPage < dots.size()) {
                View dot = dots.get(currentPage);

                if (dot != null) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dot.getLayoutParams();
                    params.width = (int) dotsSize;
                    dot.setLayoutParams(params);
                }
            }

            currentPage = viewPager.getCurrentItem();
            if (currentPage >= dots.size()) {
                currentPage = dots.size() - 1;
                viewPager.setCurrentItem(currentPage, false);
            }
            View dot = dots.get(currentPage);

            if (dot != null) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dot.getLayoutParams();
                params.width = isAllDot ? (int) dotsSize : (int) (dotsSize * dotsWidthFactor);
                dot.setLayoutParams(params);
            }

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
        if (viewPager2 != null
                && viewPager2.getAdapter() != null
                && viewPager2.getAdapter().getItemCount() > 0) {
            if (currentPage < dots.size()) {
                View dot = dots.get(currentPage);

                if (dot != null) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dot.getLayoutParams();
                    params.width = (int) dotsSize;
                    dot.setLayoutParams(params);
                }
            }

            currentPage = viewPager2.getCurrentItem();
            if (currentPage >= dots.size()) {
                currentPage = dots.size() - 1;
                viewPager2.setCurrentItem(currentPage, false);
            }
            View dot = dots.get(currentPage);

            if (dot != null) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dot.getLayoutParams();
                params.width = isAllDot ? (int) dotsSize : (int) (dotsSize * dotsWidthFactor);
                dot.setLayoutParams(params);
            }

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

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                calculateDotWidth(position, positionOffset);
            }

            @Override public void onPageSelected(int position) {
                setUpSelectedColors(position);
            }

            @Override public void onPageScrollStateChanged(int state) {}
        };
    }

    /**
     * Set Page change listener for Viewpager2
     */
    private void setUpOnPageChangedListener2() {
        pageChangedListener2 = new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                calculateDotWidth(position, positionOffset);
            }

            @Override public void onPageSelected(int position) {
                setUpSelectedColors(position);
            }

            @Override public void onPageScrollStateChanged(int state) {}
        };
    }

    /**
     * Calculate image width
     * @param position current position
     * @param positionOffset Value from [0, 1) indicating the offset from the page at position.
     */
    private void calculateDotWidth(int position, float positionOffset){

        if (position != currentPage && positionOffset == 0 || currentPage < position) {
            setDotWidth(dots.get(currentPage), (int) dotsSize);
            currentPage = position;
        }

        if (Math.abs(currentPage - position) > 1) {
            setDotWidth(dots.get(currentPage), (int) dotsSize);
            currentPage = position;
        }

        ImageView dot = dots.get(currentPage);

        ImageView nextDot = null;
        if (currentPage == position && currentPage + 1 < dots.size()) {
            nextDot = dots.get(currentPage + 1);
        } else if (currentPage > position) {
            nextDot = dot;
            dot = dots.get(currentPage - 1);
        }

        int dotWidth = (int) (dotsSize + (dotsSize * (dotsWidthFactor - 1) * (1 - positionOffset)));
        setDotWidth(dot, dotWidth);

        if (nextDot != null) {
            int nextDotWidth =
                    (int) (dotsSize + (dotsSize * (dotsWidthFactor - 1) * (positionOffset)));
            setDotWidth(nextDot, nextDotWidth);
        }
    }

    /**
     * Set dot width
     */
    private void setDotWidth(@NonNull ImageView dot, int dotWidth) {
        ViewGroup.LayoutParams dotParams = dot.getLayoutParams();
        dotParams.width = isAllDot ? (int) dotsSize : dotWidth;
        dot.setLayoutParams(dotParams);
    }

    /**
     * Set dot circle(stroke) color
     */
    private void setUpCircleColors(int color) {
        if (dots != null) {
            for (ImageView elevationItem : dots) {
                ((GradientDrawable) elevationItem.getBackground()).setColor(color);
            }
        }
    }
    /**
     * Set selected dot circle(stroke) color
     */
    private void setUpSelectedColors(int position) {
        if (dots != null && dots.size() > 0) {
            for (ImageView elevationItem : dots) {
                ((GradientDrawable) elevationItem.getBackground()).setColor(dotsColor);
            }
            ((GradientDrawable) dots.get(position).getBackground()).setColor(selectedDotColor);
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
                    refreshDots2();
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
                    refreshDots();
                }
            });
        }
    }

    private int dpToPx(int dp) {
        return (int) (getContext().getResources().getDisplayMetrics().density * dp);
    }


    public void setPointsColor(int color) {
        setUpCircleColors(color);
    }

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