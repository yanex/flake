package org.yanex.flaketest.flakes

import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import org.yanex.flake.AnimatedFlake
import org.yanex.flake.FlakeManager

public interface MyFlakeAnimation: AnimatedFlake {
    private companion object {
        val DELAY = 300L
    }

    override fun getAnimationOnShow(manager: FlakeManager) = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 1f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f).withDuration(DELAY)

    override fun getAnimationOnHide(manager: FlakeManager) = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 1f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f).withDuration(DELAY)

    override fun getAnimationOnHideForPrevious(manager: FlakeManager) = AlphaAnimation(0f, 1f).withDuration(DELAY)
    override fun getAnimationOnShowForPrevious(manager: FlakeManager) = AlphaAnimation(1f, 0f).withDuration(DELAY)

    private fun Animation.withDuration(ms: Long): Animation {
        duration = ms
        return this
    }
}