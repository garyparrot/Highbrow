package com.github.garyparrot.highbrow.layout.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.github.garyparrot.highbrow.R;
import com.google.android.material.appbar.AppBarLayout;

class CustomAppBarLayoutBehavior extends AppBarLayout.Behavior {
    private boolean mIsSheetTouched = false;

    public CustomAppBarLayoutBehavior() {
        super();
    }

    public CustomAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child,
                                       View directTargetChild, View target, int axes, int type) {
        // Set flag if the bottom sheet is responsible for the nested scroll.
        mIsSheetTouched = target.getId() == R.id.bottomSheet;
        // Only consider starting a nested scroll if the bottom sheet is not touched; otherwise,
        // we will let the other views do the scrolling.
        return !mIsSheetTouched
                && super.onStartNestedScroll(coordinatorLayout, child, directTargetChild,
                target, axes, type);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        // Don't accept touch stream here if the bottom sheet is touched. This will permit the
        // bottom sheet to be dragged down without interaction with the appBar. Reset on cancel.
        if (ev.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            mIsSheetTouched = false;
        }
        return !mIsSheetTouched && super.onInterceptTouchEvent(parent, child, ev);
    }
}
