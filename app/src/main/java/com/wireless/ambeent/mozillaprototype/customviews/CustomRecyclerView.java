package com.wireless.ambeent.mozillaprototype.customviews;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by Atakan on 21.11.2017.
 * This is a version of recyclerview which ignores
 */

public class CustomRecyclerView extends RecyclerView {

    private static final String TAG = "CustomRecyclerView";

    private boolean shouldIgnoreTouch = true;

    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
  //      Log.i(TAG, "onTouchEvent: shouldIgnoreTouch " + shouldIgnoreTouch);   //    UNCOMMENT THIS TO TEST
        if(shouldIgnoreTouch){
            return false;
        } else return true;
    }

    public boolean isShouldIgnoreTouch() {
        return shouldIgnoreTouch;
    }

    public void setShouldIgnoreTouch(boolean shouldIgnoreTouch) {
        this.shouldIgnoreTouch = shouldIgnoreTouch;
    }


}
