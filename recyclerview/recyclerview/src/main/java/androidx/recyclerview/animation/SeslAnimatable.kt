package androidx.recyclerview.animation

import android.animation.ValueAnimator
import kotlinx.coroutines.DisposableHandle

abstract class SeslAnimatable<T>(
    private val initialValue: T,
    private val defaultAnimationSpec: AnimationSpec
) : DisposableHandle {

    var animator: ValueAnimator? = null

    interface AnimationSpec {
        fun invoke(valueAnimator: ValueAnimator)
    }

    abstract suspend fun animateTo(targetValue: T, animationSpec: AnimationSpec)

    override fun dispose() {
        animator?.apply {
            removeAllListeners()
            cancel()
        }
    }

    fun getValue(): T {
        return animator?.animatedValue as? T ?: initialValue
    }

}
