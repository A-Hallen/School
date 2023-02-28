package com.hallen.school.ui

import android.animation.Animator
import android.content.Context
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.hallen.school.R

class CircularImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): FrameLayout(context, attrs, defStyle) {
    private var bg: Drawable? = null
    fun setOnClickListener(function: (View) -> Unit) {
        super.setOnClickListener {
            if (bg == null) startClickAnimation()
            function(this@CircularImageView)
        }
    }

    private fun startClickAnimation() {
        bgView.apply {
            alpha = 0f
            this.scaleX = 1f; this.scaleY = 1f
            visibility = View.VISIBLE
            animate().alpha(0.3f)
                .setDuration(0)
                .scaleX(2f).scaleY(2f)
                .setListener(object : Animator.AnimatorListener{
                override fun onAnimationEnd(animation: Animator?)    {
                    endAnimation(bgView)
                }
                override fun onAnimationStart(animation: Animator?)  {  }
                override fun onAnimationCancel(animation: Animator?) {  }
                override fun onAnimationRepeat(animation: Animator?) {  }

            })
        }
    }

    private fun endAnimation(view: View) {
        val short = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
        view.apply {
            alpha = 0.3f
            animate()
                .alpha(0f)
                .setDuration(short).setListener(object : Animator.AnimatorListener{
                override fun onAnimationEnd(animation: Animator?) {
                    visibility = View.GONE
                }
                override fun onAnimationStart(animation: Animator?)  {  }
                override fun onAnimationCancel(animation: Animator?) {  }
                override fun onAnimationRepeat(animation: Animator?) {  }

            })
        }
    }

    var imageView: ImageView
    private var bgView: LinearLayout




    init {
        val view = LayoutInflater.from(context).inflate(R.layout.circle_image_view, this, true)
        imageView = view.findViewById(R.id.circle_image)
        bgView = view.findViewById(R.id.circle_image_bg)
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val rect = Rect(0, 0, view.width, view.height)
                outline.setRoundRect(rect, rect.width() / 2.0f)
            }
        }
        clipToOutline = true
        if (attrs != null){
            context.withStyledAttributes(attrs, R.styleable.CircularImageView){
                bg = getDrawable(R.styleable.CircularImageView_background)
                val pad = getDimension(R.styleable.CircularImageView_padding, 5F).toInt()
                val src= getDrawable(R.styleable.CircularImageView_src)
                if (bg != null) {
                    bgView.background = bg; bgView.visibility = View.VISIBLE
                }
                if (src != null) imageView.setImageDrawable(src)
                val params = imageView.layoutParams
                (params as LayoutParams).setMargins(pad, pad, pad, pad)
                imageView.layoutParams = params
            }
        }
    }
}

class CircleImage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyle) {

    init {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val rect = Rect(0, 0, view.width, view.height)
                outline.setRoundRect(rect, rect.width() / 2.0f)
            }
        }
        clipToOutline = true
    }
}
