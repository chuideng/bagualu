package com.chuideng.magicalview;

import android.content.Context;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

public class OverBoundFrameLayout extends FrameLayout {
    private final int OVER_SCROLL_MAX_DEFAULT = 400;
    private final float STICK_FACTOR_DEFAULT = 0.6f;
    private Scroller scroller;
    private int overScrollMax;
    private float stickFactor ;
    private float lastTouchY;
    private boolean isTouchDown;

    public OverBoundFrameLayout(Context context) {
        this(context, null);

    }

    public OverBoundFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        scroller = new Scroller(getContext());
        overScrollMax = OVER_SCROLL_MAX_DEFAULT;
        stickFactor = STICK_FACTOR_DEFAULT;

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(!scroller.isFinished()){
            return true;
        }

        View childView =  getChildAt(0);
        childView.setOverScrollMode(OVER_SCROLL_NEVER);
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:

            case MotionEvent.ACTION_POINTER_DOWN:
                if(ev.getPointerId(ev.getActionIndex()) == 0){
                    lastTouchY = ev.getRawY();
                    isTouchDown = true;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if(ev.getPointerId(ev.getActionIndex()) == 0){
                    if(isTouchDown && (isTopInterceptEvent(lastTouchY,ev.getRawY()) || isBottomInterceptEvent(lastTouchY,ev.getRawY()))){
                        int deltaScrollY = (int)((ev.getRawY() - lastTouchY) * stickFactor);
                        if(deltaScrollY == 0 && ev.getRawY() - lastTouchY > 0){
                            deltaScrollY = 1;
                        }else if(deltaScrollY == 0 && ev.getRawY() - lastTouchY <= 0){
                            deltaScrollY = -1;
                        }

                        if(isTopInterceptEvent(lastTouchY,ev.getRawY())){
                            if(deltaScrollY < 0){
                                if( getScrollY()-deltaScrollY < 0){
                                    scrollTo(0, getScrollY()-deltaScrollY);
                                }else{
                                    scrollTo(0, 0);
                                    long downtime = SystemClock.uptimeMillis();
                                    MotionEvent  motionEvent = MotionEvent.obtain(downtime, deltaScrollY, MotionEvent.ACTION_CANCEL, ev.getX(), ev.getY(), 0);
                                    super.dispatchTouchEvent(motionEvent);
                                    motionEvent.recycle();
                                    downtime = SystemClock.uptimeMillis();
                                    motionEvent = MotionEvent.obtain(downtime, deltaScrollY, MotionEvent.ACTION_DOWN, ev.getX(), ev.getY(), 0);
                                    super.dispatchTouchEvent(motionEvent);
                                    motionEvent.recycle();

                                }

                            }else{
                                if( getScrollY()-deltaScrollY > -overScrollMax){
                                    scrollTo(0, getScrollY()-deltaScrollY);
                                }else{
                                    scrollTo(0, -overScrollMax);
                                }

                            }
                        }else if(isBottomInterceptEvent(lastTouchY,ev.getRawY())){
                            if(deltaScrollY < 0){
                                if( getScrollY()-deltaScrollY < overScrollMax){
                                    scrollTo(0, getScrollY()-deltaScrollY);
                                }else{
                                    scrollTo(0, overScrollMax);
                                }


                            }else{
                                if( getScrollY()-deltaScrollY > 0){
                                    scrollTo(0, getScrollY()-deltaScrollY);
                                }else{
                                    scrollTo(0, 0);
                                    long downtime = SystemClock.uptimeMillis();
                                    MotionEvent  motionEvent = MotionEvent.obtain(downtime, deltaScrollY, MotionEvent.ACTION_CANCEL, ev.getX(), ev.getY(), 0);
                                    super.dispatchTouchEvent(motionEvent);
                                    motionEvent.recycle();
                                    downtime = SystemClock.uptimeMillis();
                                    motionEvent = MotionEvent.obtain(downtime, deltaScrollY, MotionEvent.ACTION_DOWN, ev.getX(), ev.getY(), 0);
                                    super.dispatchTouchEvent(motionEvent);
                                    motionEvent.recycle();
                                }
                            }
                        }


                    }
                    lastTouchY = ev.getRawY();

                    if(isTouchDown && getScrollY() != 0){
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if(ev.getPointerId(ev.getActionIndex()) == 0){
                    if(isTouchDown && getScrollY() != 0){
                        scroller.startScroll(0,getScrollY(),0,-getScrollY(),300);
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
        if(scroller.computeScrollOffset()){
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        }
    }


    private boolean isTopInterceptEvent(float lastTouchY,float currentTouchY){

        final View childView =  getChildAt(0);
        if(childView instanceof ScrollView){
            if ((currentTouchY - lastTouchY) >= 0 && childView.getScrollY() == 0) {
                return true;
            }

            if ((currentTouchY - lastTouchY) < 0 && childView.getScrollY() == 0 && getScrollY() < 0) {
                return true;
            }

        }

        if(childView instanceof RecyclerView){
            final RecyclerView recyclerView = (RecyclerView) childView;
            final LinearLayoutManager  layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if ((currentTouchY - lastTouchY) >= 0 && layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                return true;
            }

            if ((currentTouchY - lastTouchY) < 0 && layoutManager.findFirstCompletelyVisibleItemPosition() == 0 && getScrollY() < 0) {
                return true;
            }

        }

        if(childView instanceof AbsListView){

            final AbsListView absListView = (AbsListView) childView;

            final int firstVisiblePosition = absListView.getFirstVisiblePosition();
            if(absListView.getChildCount()  == 0){
                return true;
            }

            if ((currentTouchY - lastTouchY) > 0 && (firstVisiblePosition == 0 && absListView.getChildAt(0).getTop() == 0)) {
                return true;
            }

            if ((currentTouchY - lastTouchY) <= 0 && (firstVisiblePosition == 0 && absListView.getChildAt(0).getTop() == 0) && getScrollY() < 0) {
                return true;
            }

        }
        return false;
    }

    private boolean isBottomInterceptEvent(float lastTouchY,float currentTouchY){

        final View childView =  getChildAt(0);
        if(childView instanceof ScrollView){

            final ScrollView scrollView = (ScrollView) childView;
            if ((currentTouchY - lastTouchY) <= 0 && scrollView.getChildAt(0).getMeasuredHeight() == childView.getScrollY() + childView.getHeight()) {
                return true;
            }

            if ((currentTouchY - lastTouchY) > 0 && scrollView.getChildAt(0).getMeasuredHeight() == childView.getScrollY() + childView.getHeight() && getScrollY() > 0) {
                return true;
            }
        }

        if(childView instanceof RecyclerView){
            final RecyclerView recyclerView = (RecyclerView) childView;
            final LinearLayoutManager  layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            if ((currentTouchY - lastTouchY) <= 0 && layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() -1) {
                return true;
            }

            if ((currentTouchY - lastTouchY) > 0 && layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() -1 && getScrollY() >0) {
                return true;
            }
        }

        if(childView instanceof AbsListView){

            final AbsListView absListView = (AbsListView) childView;

            if(absListView.getChildCount()  == 0){
                return true;
            }

            final int lastVisiblePosition = absListView.getLastVisiblePosition();
            if ((currentTouchY - lastTouchY) <= 0 &&  lastVisiblePosition == absListView.getAdapter().getCount()-1 && absListView.getChildAt(absListView.getChildCount()-1).getBottom() <= absListView.getHeight()) {
                return true;
            }

            if ((currentTouchY - lastTouchY) > 0 &&  lastVisiblePosition == absListView.getAdapter().getCount()-1 && absListView.getChildAt(absListView.getChildCount()-1).getBottom() <= absListView.getHeight() && getScrollY()  > 0) {
                return true;
            }

        }
        return false;
    }



}
