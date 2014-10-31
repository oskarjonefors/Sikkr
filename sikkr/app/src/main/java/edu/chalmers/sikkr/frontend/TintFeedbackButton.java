package edu.chalmers.sikkr.frontend;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class TintFeedbackButton extends Button {

    public TintFeedbackButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TintFeedbackButton(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final Collection<Drawable> drawables = new ArrayList<>();
        final int color = 0xff888888;
        Drawable[] drawableArray = getCompoundDrawables();
        drawables.addAll(Arrays.asList(drawableArray));

        if (getBackground() != null)
            drawables.add(getBackground());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (Drawable draw : drawables) {
                    if (draw != null)
                        draw.setColorFilter(new LightingColorFilter(color, color));
                }

                setTextColor(color);

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                    /* clear color filter */
                for (Drawable draw : drawables) {
                    if (draw != null)
                        draw.setColorFilter(null);
                }

                setTextColor(Color.BLACK);

                break;
        }
        return super.onTouchEvent(event);
    }
}
