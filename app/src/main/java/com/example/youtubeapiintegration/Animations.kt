package com.example.youtubeapiintegration

import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView

class Animations {

    fun runLayoutAnimation(recyclerView: RecyclerView) {

        val context = recyclerView.context
        val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)

        recyclerView.layoutAnimation = controller
        recyclerView.adapter!!.notifyDataSetChanged()
        recyclerView.scheduleLayoutAnimation()
    }
}
