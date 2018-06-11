package com.wireless.ambeent.mozillaprototype.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Created by Ambeent Wireless.
 * Standard EditText with one extra feature. It loses focus on "back pressed"
 */

public class EditTextV2 extends android.support.v7.widget.AppCompatEditText
{

    private String savedText;

    public EditTextV2( Context context )
    {
        super( context );
    }

    public EditTextV2( Context context, AttributeSet attribute_set )
    {
        super( context, attribute_set );
    }

    public EditTextV2( Context context, AttributeSet attribute_set, int def_style_attribute )
    {
        super( context, attribute_set, def_style_attribute );
    }



    @Override
    public boolean onKeyPreIme( int key_code, KeyEvent event )
    {

        if ( event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP ){
            savedText = getText().toString();
            this.clearFocus();
            setText("");
        }


        return super.onKeyPreIme( key_code, event );
    }

    public String getSavedText(){

        if(savedText == null){
            return "";
        }else return savedText;

    }
}
