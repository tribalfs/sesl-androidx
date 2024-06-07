/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.ui.test

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/**
 * The receiver scope of the multi-modal input injection lambda from [performMultiModalInput].
 *
 * [MultiModalInjectionScope] brings together the receiver scopes of all individual modalities,
 * allowing you to inject gestures that consist of events from different modalities, like touch and
 * mouse. For each modality, there is a function to which you pass a lambda in which you can inject
 * events for that modality: currently, we have [touch], [mouse] and [key] functions. See their
 * respective docs for more information.
 *
 * Note that all events generated by the gesture methods are batched together and sent as a whole
 * after [performMultiModalInput] has executed its code block.
 *
 * Example of performing a click via touch input followed by drag and drop via mouse input:
 *
 * @sample androidx.compose.ui.test.samples.multiModalInputClickDragDrop
 * @see InjectionScope
 * @see TouchInjectionScope
 * @see MouseInjectionScope
 * @see KeyInjectionScope
 * @see RotaryInjectionScope
 */
// TODO(fresen): add better multi modal example when we have key input support
sealed interface MultiModalInjectionScope : InjectionScope {
    /** Injects all touch events sent by the given [block] */
    fun touch(block: TouchInjectionScope.() -> Unit)

    /** Injects all mouse events sent by the given [block] */
    fun mouse(block: MouseInjectionScope.() -> Unit)

    /** Injects all key events sent by the given [block] */
    @ExperimentalTestApi fun key(block: KeyInjectionScope.() -> Unit)

    /** Injects all rotary events sent by the given [block] */
    @ExperimentalTestApi fun rotary(block: RotaryInjectionScope.() -> Unit)
}

internal class MultiModalInjectionScopeImpl(node: SemanticsNode, testContext: TestContext) :
    MultiModalInjectionScope, Density by node.layoutInfo.density {
    // TODO(b/133217292): Better error: explain which gesture couldn't be performed
    private var _semanticsNode: SemanticsNode? = node
    private val semanticsNode
        get() =
            checkNotNull(_semanticsNode) {
                "Can't query SemanticsNode, InjectionScope has already been disposed"
            }

    // TODO(b/133217292): Better error: explain which gesture couldn't be performed
    private var _inputDispatcher: InputDispatcher? =
        createInputDispatcher(
            testContext,
            checkNotNull(semanticsNode.root) { "null semantics root" }
        )
    internal val inputDispatcher
        get() =
            checkNotNull(_inputDispatcher) {
                "Can't send gesture, InjectionScope has already been disposed"
            }

    /**
     * Returns and stores the visible bounds of the [semanticsNode] we're interacting with. This
     * applies clipping, which is almost always the correct thing to do when injecting gestures, as
     * gestures operate on visible UI.
     */
    private val boundsInRoot: Rect by lazy { semanticsNode.boundsInRoot }

    /**
     * Returns the size of the visible part of the node we're interacting with. This is contrary to
     * [SemanticsNode.size], which returns the unclipped size of the node.
     */
    override val visibleSize: IntSize by lazy {
        IntSize(boundsInRoot.width.roundToInt(), boundsInRoot.height.roundToInt())
    }

    /**
     * Transforms the [position] to root coordinates.
     *
     * @param position A position in local coordinates
     * @return [position] transformed to coordinates relative to the containing root.
     */
    internal fun localToRoot(position: Offset): Offset {
        return if (position.isValid()) {
            position + boundsInRoot.topLeft
        } else {
            // Allows invalid position to still pass back through Compose (for testing)
            position
        }
    }

    internal fun rootToLocal(position: Offset): Offset {
        return if (position.isValid()) {
            position - boundsInRoot.topLeft
        } else {
            // Allows invalid position to still pass back through Compose (for testing)
            position
        }
    }

    override val viewConfiguration: ViewConfiguration
        get() = semanticsNode.layoutInfo.viewConfiguration

    internal fun dispose() {
        _semanticsNode = null
        _inputDispatcher?.also {
            _inputDispatcher = null
            try {
                it.flush()
            } finally {
                it.dispose()
            }
        }
    }

    /**
     * Adds the given [durationMillis] to the current event time, delaying the next event by that
     * time. Only valid when a gesture has already been started, or when a finished gesture is
     * resumed.
     */
    override fun advanceEventTime(durationMillis: Long) {
        inputDispatcher.advanceEventTime(durationMillis)
    }

    private val touchScope: TouchInjectionScope = TouchInjectionScopeImpl(this)

    private val mouseScope: MouseInjectionScope = MouseInjectionScopeImpl(this)

    @ExperimentalTestApi private val keyScope: KeyInjectionScope = KeyInjectionScopeImpl(this)

    @ExperimentalTestApi
    private val rotaryScope: RotaryInjectionScope = RotaryInjectionScopeImpl(this)

    override fun touch(block: TouchInjectionScope.() -> Unit) {
        block.invoke(touchScope)
    }

    override fun mouse(block: MouseInjectionScope.() -> Unit) {
        block.invoke(mouseScope)
    }

    @ExperimentalTestApi
    override fun key(block: KeyInjectionScope.() -> Unit) {
        block.invoke(keyScope)
    }

    @ExperimentalTestApi
    override fun rotary(block: RotaryInjectionScope.() -> Unit) {
        block.invoke(rotaryScope)
    }
}
