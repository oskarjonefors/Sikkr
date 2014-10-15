package edu.chalmers.sikkr.frontend;

import android.content.Context;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

public class TintFeedbackButton extends Button {

    public TintFeedbackButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TintFeedbackButton(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /* index 1 in this array is the Top drawable */
        final Drawable[] drawables = getCompoundDrawables();

        if (drawables != null && drawables.length >= 1) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drawables[1].setColorFilter(new LightingColorFilter(0xff888888, 0x000000));
                    break;

                case MotionEvent.ACTION_UP:
                    /* clear color filter */
                    drawables[1].setColorFilter(null);
                    break;
            }
        }
        return super.onTouchEvent(event);
    }
}
