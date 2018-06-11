package com.wireless.ambeent.mozillaprototype.customviews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;


/**
 * Created by Ambeent Wireless.
 * This class is needed to get the touch events of map (like swipe direction)
 */

public class TouchableWrapper extends FrameLayout {

    private static final String TAG = "TouchableWrapper";

    private static final int SWIPE_DISTANCE_TRESHOLD = 120;

    private float x1, x2, y1, y2, dx, dy;
    private String direction;

    public TouchableWrapper(@NonNull Context context) {
        super(context);

        Log.i(TAG, "TouchableWrapper: ");

    }

    public TouchableWrapper(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "TouchableWrapper: ");

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //We must find the direction of swipe action here.

        switch(event.getAction()) {
            case(MotionEvent.ACTION_DOWN):
                x1 = event.getX();
                y1 = event.getY();
                break;

            case(MotionEvent.ACTION_UP): {
                x2 = event.getX();
                y2 = event.getY();
                dx = x2-x1;
                dy = y2-y1;

                Log.i(TAG, "dispatchTouchEvent dx:  " + dx + " dy: " + dy);

                double angle = getAngle(x1, y1, x2, y2);

                Log.i(TAG, "dispatchTouchEvent: angle " + angle);

                if(Math.abs(dx) > SWIPE_DISTANCE_TRESHOLD || Math.abs(dy) > SWIPE_DISTANCE_TRESHOLD){
                    // Use dx and dy to determine the direction of the move

                    if(inRange(angle, 45, 135)){
                        direction = "south";
                    }
                    else if(inRange(angle, 0,45) || inRange(angle, 315, 360)){
                        direction = "west";
                    }
                    else if(inRange(angle, 225, 315)){
                        direction = "north";
                    }
                    else{
                        direction = "east";
                    }

                   /* if(Math.abs(dx) > Math.abs(dy)) {
                        if(dx>0)
                            direction = "west";
                        else
                            direction = "east";
                    } else {
                        if(dy>0)
                            direction = "north";
                        else
                            direction = "south";
                    }*/

                    //Handle swipe according to direction
               //     ((MainActivity) getContext()).mMapSwipeHandler.handleSwipe(direction);
                }


            }
        }


        return super.dispatchTouchEvent(event);
    }

    //Calculates the angle between two points. 0/360 is the positive x axis.
    public double getAngle(float x1, float y1, float x2, float y2) {

        double rad = Math.atan2(y1-y2,x2-x1) + Math.PI;
        return (rad*180/Math.PI + 180)%360;
    }

    //returns true if the given angle is in the interval (init, end).
    private static boolean inRange(double angle, float init, float end){
        return (angle >= init) && (angle < end);
    }


}
