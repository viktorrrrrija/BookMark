package com.example.bookmark

import android.content.Context
import android.util.AttributeSet
import android.widget.RatingBar

class CustomRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.ratingBarStyle
) : androidx.appcompat.widget.AppCompatRatingBar(context, attrs, defStyleAttr) {

    private val stepSize = 0.5f

    override fun getStepSize(): Float {
        return stepSize
    }
}
