package edu.chalmers.sikkr.frontend;

import android.content.Context;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        Drawable[] drawableArray = getCompoundDrawables();
        final List<Drawable> drawables = new ArrayList<Drawable>();
        drawables.addAll(Arrays.asList(drawableArray));

        if (getBackground() != null)
            drawables.add(getBackground());



        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (Drawable draw : drawables) {
                    if (draw != null)
                        draw.setColorFilter(new LightingColorFilter(0xff888888, 0x000000));
                }
                break;

            case MotionEvent.ACTION_UP:
                    /* clear color filter */
                for (Drawable draw : drawables) {
                    if (draw != null)
                        draw.setColorFilter(null);
                }
                break;
        }
        return super.onTouchEvent(event);
    }
}