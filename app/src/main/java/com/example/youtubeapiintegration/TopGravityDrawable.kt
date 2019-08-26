package com.example.youtubeapiintegration

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable

class TopGravityDrawable(res: Resources, bitmap: Bitmap) : BitmapDrawable(res, bitmap) {

    override fun draw(canvas: Canvas) {
        val halfCanvas = bounds.height() / 2
        val halfDrawable = intrinsicHeight / 2
        canvas.save()
        canvas.translate(0f, (-halfCanvas + halfDrawable).toFloat())
        super.draw(canvas)
        canvas.restore()
    }
}
