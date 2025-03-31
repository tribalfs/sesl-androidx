package androidx.recyclerview.animation

import android.animation.ValueAnimator
import android.view.animation.Interpolator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

//Added in sesl7
abstract class SeslAnimatable<T>(
    private val initialValue: T,
    private val defaultAnimationSpec: AnimationSpec
) : DisposableHandle {

    var animator: ValueAnimator? = null

    interface AnimationSpec {
        fun invoke(valueAnimator: ValueAnimator)
    }

    abstract suspend fun animateTo(targetValue: T, animationSpec: AnimationSpec)

    abstract val onValueUpdated: (position: T) -> Unit

    override fun dispose() {
        animator?.apply {
            removeAllListeners()
            cancel()
        }
    }

    fun getValue(): T {
        @Suppress("UNCHECKED_CAST")
        return animator?.animatedValue as? T ?: initialValue
    }

    class SimpleAnimationSpec(
        val duration: Long,
        val interpolator: Interpolator
    ) : AnimationSpec {
        override fun invoke(valueAnimator: ValueAnimator) {
            valueAnimator.duration = duration
            valueAnimator.interpolator = interpolator
        }
    }

    object SnapAnimationSpec : AnimationSpec {
        override fun invoke(valueAnimator: ValueAnimator) {
            valueAnimator.duration = 0L
        }
    }

    suspend fun snapTo(targetValue: T): Unit = animateTo(targetValue, SnapAnimationSpec)

    @JvmOverloads
    fun tryAnimateTo(
        targetValue: T, animationSpec: AnimationSpec = defaultAnimationSpec,
        dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
    ): Job {
        return CoroutineScope(dispatcher).launch {
            animateTo(targetValue, animationSpec)
        }
    }

    @JvmOverloads
    fun trySnapTo(
        targetValue: T,
        dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
    ): Job {
        return CoroutineScope(dispatcher).launch {
            snapTo(targetValue)
        }
    }
}