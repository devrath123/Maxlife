package com.maxlife.utils;

/**
 * Created by anshul.mittal on 23/6/16.
 */
import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class CustomVideo extends VideoView {

    int height;
    int width;

    public CustomVideo(Context context) {
        super(context);
    }

    public CustomVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideo(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setFixedVideoSize(int width, int height) {
        this.width = width;
        this.height = height;
        getHolder().setFixedSize(width, height);
        requestLayout();
        invalidate();
    }
}
