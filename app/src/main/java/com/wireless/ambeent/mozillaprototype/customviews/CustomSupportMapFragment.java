package com.wireless.ambeent.mozillaprototype.customviews;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by Ambeent Wireless.
 */

public class CustomSupportMapFragment extends SupportMapFragment {

    private View mOriginalContentView;
    private TouchableWrapper mTouchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState);
        mTouchView = new TouchableWrapper(getActivity());
        mTouchView.addView(mOriginalContentView);
        return mTouchView;

    }

    @Override
    public View getView() {
        return mOriginalContentView;
    }

    public TouchableWrapper getWrapper(){
        return mTouchView;
    }



}
