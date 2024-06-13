package com.example.sit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class CustomViewPager extends ViewPager {
    private float x1, x2;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;
                if (Math.abs(deltaX) > 5) {
                    if (x2 > x1) {
                        // Chuyển sang trang trước
                        if (getCurrentItem() > 0) {
                            setCurrentItem(getCurrentItem() - 1);
                        }
                    } else {
                        // Chuyển sang trang kế tiếp
                        if (getCurrentItem() < getAdapter().getCount() - 1) {
                            setCurrentItem(getCurrentItem() + 1);
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }
}