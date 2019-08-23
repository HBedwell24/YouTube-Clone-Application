package com.example.youtubeapiintegration;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

public class TopGravityDrawable extends BitmapDrawable {

    public TopGravityDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    @Override
    public void draw(Canvas canvas) {
        int halfCanvas = canvas.getHeight() / 2;
        int halfDrawable = getIntrinsicHeight() / 2;
        canvas.save();
        canvas.translate(0, -halfCanvas + halfDrawable);
        super.draw(canvas);
        canvas.restore();
    }
}
