package com.chuideng.magicalview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

public class OverBoundScrollView extends ScrollView {
    private final int OVER_SCROLL_MAX_DEFAULT = 400;
    private final float STICK_FACTOR_DEFAULT = 0.6f;
    private Scroller scroller;
    private int overScrollMax;
    private float stickFactor = 0.3f;
    private float downY;
    private float downScrollY;
    private boolean isTouchDown;

    public OverBoundScrollView(Context context) {
        this(context, null);

    }

    public OverBoundScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        scroller = new Scroller(getContext());
        overScrollMax = OVER_SCROLL_MAX_DEFAULT;
        stickFactor = STICK_FACTOR_DEFAULT;

    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        if(!scroller.isFinished()){
            return true;
        }

        final LinearLayout childView = (LinearLayout) getChildAt(0);
        setOverScrollMode(OVER_SCROLL_NEVER);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = ev.getRawY();
                downScrollY = getScrollY();
                isTouchDown = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if(isTouchDown){
                    if ((ev.getRawY() - downY) >= 0 && getScrollY() != 0) {
                        return super.dispatchTouchEvent(ev);
                    }
                    if ((ev.getRawY() - downY) <= 0 && getChildAt(0).getMeasuredHeight() != getScrollY() + getHeight()) {
                        return super.dispatchTouchEvent(ev);
                    }

                    int scrollY = (int)((downY - ev.getRawY() - (getScrollY() - downScrollY)) * stickFactor);
                    if(Math.abs(scrollY) <= overScrollMax){
                        childView.scrollTo(0, scrollY);

                    }else{
                        if(scrollY > 0){
                            childView.scrollTo(0, overScrollMax);
                        }else{
                            childView.scrollTo(0, -overScrollMax);
                        }
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(isTouchDown){
                    if(childView.getScrollY() !=  0){
                        scroller.startScroll(0,childView.getScrollY(),0,-childView.getScrollY(),300);
                        isTouchDown = false;
                        invalidate();
                        return true;
                    }
                }

                break;

        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if(scroller.computeScrollOffset()){
            getChildAt(0).scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        }
    }
}
