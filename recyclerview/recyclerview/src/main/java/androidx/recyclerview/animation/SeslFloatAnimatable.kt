package androidx.recyclerview.animation

import android.animation.Animator
import android.animation.ValueAnimator
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.suspendCancellableCoroutine


//Added in sesl7
class SeslFloatAnimatable(
    initialValue: Float,
    defaultAnimationSpec: AnimationSpec,
    override val onValueUpdated: (position: Float) -> Unit
) : SeslAnimatable<Float>(initialValue, defaultAnimationSpec), DisposableHandle {

    override suspend fun animateTo(targetValue: Float, animationSpec: AnimationSpec) {
        suspendCancellableCoroutine { continuation ->
            dispose()

            if (getValue() != targetValue) {
                animator = ValueAnimator.ofFloat(getValue(), targetValue).apply {
                    animationSpec.invoke(this)
                    addUpdateListener { animator ->
                        val animatedValue = animator.animatedValue as Float
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

    override fun dispose() {
        animator?.apply {
            removeAllListeners()
            cancel()
        }
    }
}

