package jadkowski.dawid.pollutionapp.custom;

/**
 * Created by dawid on 29/01/2016.
 * Class reused https://stackoverflow.com/questions/16605486/edit-text-not-scrollable-inside-scroll-view
 *
 * @Sandeep the author
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class VerticalScrollview extends ScrollView {
    //implement all three constructors of ScrollView
    public VerticalScrollview(Context context) {
        super(context);
    }

    public VerticalScrollview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalScrollview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        //intercept the motion, return true if want scrolling to happen, else false
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                super.onTouchEvent(ev);
                break;

            case MotionEvent.ACTION_MOVE:
                return false; // redirect MotionEvents to ourself

            case MotionEvent.ACTION_CANCEL:
                super.onTouchEvent(ev);
                break;

            case MotionEvent.ACTION_UP:
                return false;

            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        return true;
    }
}