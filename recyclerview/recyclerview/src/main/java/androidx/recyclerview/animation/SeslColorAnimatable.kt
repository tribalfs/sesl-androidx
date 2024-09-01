package androidx.recyclerview.animation

import android.animation.Animator
import android.animation.ValueAnimator
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine


class SeslColorAnimatable(
    initialValue: Int,
    defaultAnimationSpec: SeslAnimatable.AnimationSpec,
    private val onValueUpdated: (Int) -> Unit
) : SeslAnimatable<Int>(initialValue, defaultAnimationSpec) {

    override suspend fun animateTo(targetValue: Int, animationSpec: SeslAnimatable.AnimationSpec) {
        suspendCancellableCoroutine { continuation ->
            dispose()
            if (getValue() != targetValue) {
                animator =  ValueAnimator.ofArgb(getValue(), targetValue).apply {
                    animationSpec.invoke(this)
                    addUpdateListener { animator ->
                        val animatedValue = animator.animatedValue as Int
                        onValueUpdated(animatedValue)
                    }
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animator: Animator) {}
                        override fun onAnimationEnd(animator: Animator) {
                            continuation.resume(Unit)
                        }
                        override fun onAnimationCancel(animator: Animator) {
                            continuation.resumeWithException(CancellationException("Animation cancelled"))
                        }
                        override fun onAnimationRepeat(animator: Animator) {}
                    })
                    start()
                }
            } else {
                continuation.resume(Unit)
            }
        }
    }
}