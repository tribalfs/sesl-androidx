/*
 * Copyright 2024 The Android Open Source Project
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

package androidx.compose.material3

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.internal.ProvideContentColorTextStyle
import androidx.compose.material3.tokens.MotionSchemeKeyTokens
import androidx.compose.material3.tokens.ShapeTokens
import androidx.compose.material3.tokens.SplitButtonSmallTokens
import androidx.compose.material3.tokens.StateTokens
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastMaxOfOrNull
import androidx.compose.ui.util.fastSumBy
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * A [SplitButton] let user define a button group consisting of 2 buttons. The leading button
 * performs a primary action, and the trailing button performs a secondary action that is
 * contextually related to the primary action.
 *
 * @sample androidx.compose.material3.samples.SplitButtonSample
 * @sample androidx.compose.material3.samples.SplitButtonWithTextSample
 * @sample androidx.compose.material3.samples.SplitButtonWithIconSample
 *
 * Choose the best split button for an action based on the amount of emphasis it needs. The more
 * important an action is, the higher emphasis its button should be.
 *
 * @param leadingButton the leading button. You can specify your own composable or construct a
 *   [SplitButtonDefaults.LeadingButton]
 * @param trailingButton the trailing button.You can specify your own composable or construct a
 *   [SplitButtonDefaults.TrailingButton]
 * @param modifier the [Modifier] to be applied to this split button.
 * @param spacing The spacing between the [leadingButton] and [trailingButton]
 * @see FilledSplitButton for a high-emphasis split button without a shadow.
 * @see OutlinedSplitButton for a medium-emphasis split button with a border.
 * @see TonalSplitButton for a middle ground between [OutlinedSplitButton] and [FilledSplitButton].
 * @see ElevatedSplitButton for an [TonalSplitButton] with a shadow.
 */
@ExperimentalMaterial3ExpressiveApi
@Composable
fun SplitButton(
    leadingButton: @Composable () -> Unit,
    trailingButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = SplitButtonDefaults.Spacing,
) {
    SplitButtonLayout(
        leadingButton,
        trailingButton,
        spacing,
        modifier.minimumInteractiveComponentSize()
    )
}

/**
 * A [SplitButton] let user define a button group consisting of 2 buttons. The leading button
 * performs a primary action, and the trailing button performs a secondary action that is
 * contextually related to the primary action.
 *
 * Filled split button is the high-emphasis version of split button. It should be used for
 * emphasizing important or final actions.
 *
 * @sample androidx.compose.material3.samples.FilledSplitButtonSample
 *
 * Choose the best split button for an action based on the amount of emphasis it needs. The more
 * important an action is, the higher emphasis its button should be.
 *
 * @param onLeadingButtonClick called when the leading button is clicked
 * @param onTrailingButtonClick called when the trailing button is clicked
 * @param leadingContent the content to be placed inside the leading button. A container will be
 *   provided internally to offer the standard design and style for a [FilledSplitButton].
 * @param trailingContent the content to be placed inside the trailing button. A container is
 *   provided internally to ensure the standard design and style of a [FilledSplitButton]. The
 *   container corner radius morphs to `full` when the [checked] state changes to `true`.
 * @param checked indicates if the trailing button is toggled. This can be used to indicate a new
 *   state that's a result of [onTrailingButtonClick]. For example, a drop down menu or pop up.
 * @param modifier the [Modifier] to be applied to this this split button.
 * @param enabled controls the enabled state of the split button. When `false`, this component will
 *   not respond to user input, and it will appear visually disabled and disabled to accessibility
 *   services.
 * @param innerCornerSize The size for leading button's end corners and trailing button's start
 *   corners
 * @param spacing The spacing between the leading and trailing buttons
 * @see OutlinedSplitButton for a medium-emphasis split button with a border.
 * @see TonalSplitButton for a middle ground between [OutlinedSplitButton] and [FilledSplitButton].
 * @see ElevatedSplitButton for an [TonalSplitButton] with a shadow.
 *
 * The default text style for internal [Text] components will be set to [Typography.labelLarge].
 */
@ExperimentalMaterial3ExpressiveApi
@Composable
fun FilledSplitButton(
    onLeadingButtonClick: () -> Unit,
    onTrailingButtonClick: () -> Unit,
    leadingContent: @Composable RowScope.() -> Unit,
    trailingContent: @Composable RowScope.() -> Unit,
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    innerCornerSize: CornerSize = SplitButtonDefaults.InnerCornerSize,
    spacing: Dp = SplitButtonDefaults.Spacing
) {
    SplitButton(
        modifier = modifier,
        spacing = spacing,
        leadingButton = {
            SplitButtonDefaults.LeadingButton(
                onClick = onLeadingButtonClick,
                enabled = enabled,
                shapes = SplitButtonDefaults.leadingButtonShapes(innerCornerSize),
                content = leadingContent
            )
        },
        trailingButton = {
            SplitButtonDefaults.TrailingButton(
                onClick = onTrailingButtonClick,
                modifier = Modifier,
                enabled = enabled,
                checked = checked,
                shapes =
                    SplitButtonDefaults.trailingButtonShapes(startCornerSize = innerCornerSize),
                content = trailingContent,
            )
        },
    )
}

/**
 * A [SplitButton] let user define a button group consisting of 2 buttons. The leading button
 * performs a primary action, and the trailing button performs a secondary action that is
 * contextually related to the primary action.
 *
 * Tonal split button is the medium-emphasis version of split buttons. It's a middle ground between
 * [FilledSplitButton] and [OutlinedSplitButton]
 *
 * @sample androidx.compose.material3.samples.TonalSplitButtonSample
 *
 * Choose the best split button for an action based on the amount of emphasis it needs. The more
 * important an action is, the higher emphasis its button should be.
 *
 * @param onLeadingButtonClick called when the leading button is clicked
 * @param onTrailingButtonClick called when the trailing button is clicked
 * @param leadingContent the content to be placed inside the leading button. A container will be
 *   provided internally to offer the standard design and style for a [TonalSplitButton].
 * @param trailingContent the content to be placed inside the trailing button. A container is
 *   provided internally to ensure the standard design and style of a [TonalSplitButton]. The
 *   container corner radius morphs to full when the [checked] state changes to `true`.
 * @param checked indicates if the trailing button is toggled. This can be used to indicate a new
 *   state that's a result of [onTrailingButtonClick]. For example, a drop down menu or pop up.
 * @param modifier the [Modifier] to be applied to this split button.
 * @param enabled controls the enabled state of the split button. When `false`, this component will
 *   not respond to user input, and it will appear visually disabled and disabled to accessibility
 *   services.
 * @param innerCornerSize The size for leading button's end corners and trailing button's start
 *   corners
 * @param spacing The spacing between the leading and trailing buttons
 * @see FilledSplitButton for a high-emphasis split button without a shadow.
 * @see OutlinedSplitButton for a medium-emphasis split button with a border.
 * @see ElevatedSplitButton for an [TonalSplitButton] with a shadow.
 *
 * The default text style for internal [Text] components will be set to [Typography.labelLarge].
 */
@ExperimentalMaterial3ExpressiveApi
@Composable
fun TonalSplitButton(
    onLeadingButtonClick: () -> Unit,
    onTrailingButtonClick: () -> Unit,
    leadingContent: @Composable RowScope.() -> Unit,
    trailingContent: @Composable RowScope.() -> Unit,
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    innerCornerSize: CornerSize = SplitButtonDefaults.InnerCornerSize,
    spacing: Dp = SplitButtonDefaults.Spacing
) {
    SplitButton(
        leadingButton = {
            TonalLeadingButton(
                onClick = onLeadingButtonClick,
                modifier = Modifier,
                enabled = enabled,
                shapes = SplitButtonDefaults.leadingButtonShapes(endCornerSize = innerCornerSize),
                content = leadingContent,
            )
        },
        trailingButton = {
            TonalTrailingButton(
                onClick = onTrailingButtonClick,
                modifier = Modifier,
                enabled = enabled,
                checked = checked,
                shapes = SplitButtonDefaults.trailingButtonShapes(innerCornerSize),
                content = trailingContent,
            )
        },
        modifier = modifier,
        spacing = spacing
    )
}

/**
 * A [SplitButton] let user define a button group consisting of 2 buttons. The leading button
 * performs a primary action, and the trailing button performs a secondary action that is
 * contextually related to the primary action.
 *
 * Elevated split buttons are essentially [OutlinedSplitButton]s with a shadow. To prevent shadow
 * creep, only use them when absolutely necessary, such as when the button requires visual
 * separation from patterned container.
 *
 * @sample androidx.compose.material3.samples.ElevatedSplitButtonSample
 *
 * Choose the best split button for an action based on the amount of emphasis it needs. The more
 * important an action is, the higher emphasis its button should be.
 *
 * @param onLeadingButtonClick called when the leading button is clicked
 * @param onTrailingButtonClick called when the trailing button is clicked
 * @param leadingContent the content to be placed inside the leading button. A container will be
 *   provided internally to offer the standard design and style for a [ElevatedSplitButton].
 * @param trailingContent the content to be placed inside the trailing button. A container is
 *   provided internally to ensure the standard design and style of a [ElevatedSplitButton]. The
 *   container corner radius morphs to full when the [checked] state changes to `true`.
 * @param checked indicates if the trailing button is toggled. This can be used to indicate a new
 *   state that's a result of [onTrailingButtonClick]. For example, a drop down menu or pop up.
 * @param modifier the [Modifier] to be applied to this split button.
 * @param enabled controls the enabled state of the split button. When `false`, this component will
 *   not respond to user input, and it will appear visually disabled and disabled to accessibility
 *   services.
 * @param innerCornerSize The size for leading button's end corners and trailing button's start
 *   corners
 * @param spacing The spacing between the leading and trailing buttons
 * @see FilledSplitButton for a high-emphasis split button without a shadow.
 * @see OutlinedSplitButton for a medium-emphasis split button with a border.
 * @see TonalSplitButton for a middle ground between [OutlinedSplitButton] and [FilledSplitButton].
 *
 * The default text style for internal [Text] components will be set to [Typography.labelLarge].
 */
@ExperimentalMaterial3ExpressiveApi
@Composable
fun ElevatedSplitButton(
    onLeadingButtonClick: () -> Unit,
    onTrailingButtonClick: () -> Unit,
    leadingContent: @Composable RowScope.() -> Unit,
    trailingContent: @Composable RowScope.() -> Unit,
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    innerCornerSize: CornerSize = SplitButtonDefaults.InnerCornerSize,
    spacing: Dp = SplitButtonDefaults.Spacing
) {
    SplitButton(
        leadingButton = {
            ElevatedLeadingButton(
                onClick = onLeadingButtonClick,
                modifier = Modifier,
                enabled = enabled,
                shapes = SplitButtonDefaults.leadingButtonShapes(endCornerSize = innerCornerSize),
                content = leadingContent,
            )
        },
        trailingButton = {
            ElevatedTrailingButton(
                onClick = onTrailingButtonClick,
                modifier = Modifier,
                enabled = enabled,
                checked = checked,
                shapes =
                    SplitButtonDefaults.trailingButtonShapes(startCornerSize = innerCornerSize),
                content = trailingContent
            )
        },
        modifier = modifier,
        spacing = spacing
    )
}

/**
 * A [SplitButton] let user define a button group consisting of 2 buttons. The leading button
 * performs a primary action, and the trailing button performs a secondary action that is
 * contextually related to the primary action.
 *
 * Outlined split buttons are medium-emphasis split buttons that are essentially
 * [OutlinedSplitButton]s with a shadow. They contain actions that are important, but aren’t the
 * primary action in an app.
 *
 * @sample androidx.compose.material3.samples.OutlinedSplitButtonSample
 *
 * Choose the best split button for an action based on the amount of emphasis it needs. The more
 * important an action is, the higher emphasis its button should be.
 *
 * @param onLeadingButtonClick called when the leading button is clicked
 * @param onTrailingButtonClick called when the trailing button is clicked
 * @param leadingContent the content to be placed inside the leading button. A container will be
 *   provided internally to offer the standard design and style for a [OutlinedSplitButton].
 * @param trailingContent the content to be placed inside the trailing button. A container is
 *   provided internally to ensure the standard design and style of a [OutlinedSplitButton]. The
 *   container corner radius morphs to full when the [checked] state changes to `true`.
 * @param checked indicates if the trailing button is toggled. This can be used to indicate a new
 *   state that's a result of [onTrailingButtonClick]. For example, a drop down menu or pop up.
 * @param modifier the [Modifier] to be applied to this split button.
 * @param enabled controls the enabled state of the split button. When `false`, this component will
 *   not respond to user input, and it will appear visually disabled and disabled to accessibility
 *   services.
 * @param innerCornerSize The size for leading button's end corners and trailing button's start
 *   corners
 * @param spacing The spacing between the leading and trailing buttons
 * @see FilledSplitButton for a high-emphasis split button without a shadow.
 * @see TonalSplitButton for a middle ground between [OutlinedSplitButton] and [FilledSplitButton].
 * @see ElevatedSplitButton for an [TonalSplitButton] with a shadow.
 *
 * The default text style for internal [Text] components will be set to [Typography.labelLarge].
 */
@ExperimentalMaterial3ExpressiveApi
@Composable
fun OutlinedSplitButton(
    onLeadingButtonClick: () -> Unit,
    onTrailingButtonClick: () -> Unit,
    leadingContent: @Composable RowScope.() -> Unit,
    trailingContent: @Composable RowScope.() -> Unit,
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    innerCornerSize: CornerSize = SplitButtonDefaults.InnerCornerSize,
    spacing: Dp = SplitButtonDefaults.Spacing
) {
    SplitButton(
        leadingButton = {
            OutlinedLeadingButton(
                onClick = onLeadingButtonClick,
                modifier = Modifier,
                enabled = enabled,
                shapes = SplitButtonDefaults.leadingButtonShapes(innerCornerSize),
                content = leadingContent,
            )
        },
        trailingButton = {
            OutlinedTrailingButton(
                onClick = onTrailingButtonClick,
                modifier = Modifier,
                enabled = enabled,
                shapes = SplitButtonDefaults.trailingButtonShapes(innerCornerSize),
                checked = checked,
                content = trailingContent
            )
        },
        modifier = modifier,
        spacing = spacing
    )
}

@Composable
private fun SplitButtonLayout(
    leadingButton: @Composable () -> Unit,
    trailingButton: @Composable () -> Unit,
    spacing: Dp,
    modifier: Modifier = Modifier,
) {
    Layout(
        {
            // Override min component size enforcement to avoid create extra padding internally
            // Enforce it on the parent instead
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Box(
                    modifier = Modifier.layoutId(LeadingButtonLayoutId),
                    contentAlignment = Alignment.Center,
                    content = { leadingButton() }
                )
                Box(
                    modifier = Modifier.layoutId(TrailingButtonLayoutId),
                    contentAlignment = Alignment.Center,
                    content = { trailingButton() }
                )
            }
        },
        modifier,
        measurePolicy = { measurables, constraints ->
            val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

            val leadingButtonPlaceable =
                measurables
                    .fastFirst { it.layoutId == LeadingButtonLayoutId }
                    .measure(looseConstraints)

            val trailingButtonPlaceable =
                measurables
                    .fastFirst { it.layoutId == TrailingButtonLayoutId }
                    .measure(
                        looseConstraints
                            .offset(
                                horizontal = -(leadingButtonPlaceable.width + spacing.roundToPx())
                            )
                            .copy(
                                minHeight = leadingButtonPlaceable.height,
                                maxHeight = leadingButtonPlaceable.height
                            )
                    )

            val placeables = listOf(leadingButtonPlaceable, trailingButtonPlaceable)

            val contentWidth = placeables.fastSumBy { it.width } + spacing.roundToPx()
            val contentHeight = placeables.fastMaxOfOrNull { it.height } ?: 0

            val width = constraints.constrainWidth(contentWidth)
            val height = constraints.constrainHeight(contentHeight)

            layout(width, height) {
                leadingButtonPlaceable.placeRelative(0, 0)
                trailingButtonPlaceable.placeRelative(
                    x = leadingButtonPlaceable.width + spacing.roundToPx(),
                    y = 0
                )
            }
        }
    )
}

// TODO Replace default value with tokens
/** Contains default values used by [SplitButton] and its style variants. */
@ExperimentalMaterial3ExpressiveApi
object SplitButtonDefaults {
    /** Default icon size for the leading button */
    val LeadingIconSize = 20.dp

    /** Default icon size for the trailing button */
    val TrailingIconSize = 22.dp

    /** Default spacing between the `leading` and `trailing` button */
    val Spacing = SplitButtonSmallTokens.BetweenSpace

    /** Default size for the leading button end corners and trailing button start corners */
    val InnerCornerSize = CornerSize(4.dp)

    private val InnerCornerSizePressed = CornerSize(8.dp)

    /**
     * Default percentage size for the leading button start corners and trailing button end corners
     */
    val OuterCornerSize = CornerSize(50)

    /** Default content padding of the leading button */
    val LeadingButtonContentPadding = PaddingValues(16.dp, 10.dp, 12.dp, 10.dp)

    /** Default content padding of the trailing button */
    val TrailingButtonContentPadding = PaddingValues(horizontal = 13.dp)

    /**
     * Default minimum width of the [LeadingButton], applies to all 4 variants of the split button
     */
    private val LeadingButtonMinWidth = 48.dp

    /**
     * Default minimum height of the split button. This applies to both [LeadingButton] and
     * [TrailingButton]. Applies to all 4 variants of the split button
     */
    private val MinHeight = SplitButtonSmallTokens.ContainerHeight

    /** Default minimum width of the [TrailingButton]. */
    private val TrailingButtonMinWidth = LeadingButtonMinWidth

    /** Trailng button state layer alpha when in checked state */
    private const val TrailingButtonStateLayerAlpha = StateTokens.PressedStateLayerOpacity

    /** Default shape of the leading button. */
    private fun leadingButtonShape(endCornerSize: CornerSize = InnerCornerSize) =
        RoundedCornerShape(OuterCornerSize, endCornerSize, endCornerSize, OuterCornerSize)

    private val LeadingPressedShape =
        RoundedCornerShape(
            topStart = OuterCornerSize,
            bottomStart = OuterCornerSize,
            topEnd = InnerCornerSizePressed,
            bottomEnd = InnerCornerSizePressed
        )
    private val TrailingPressedShape =
        RoundedCornerShape(
            topStart = InnerCornerSizePressed,
            bottomStart = InnerCornerSizePressed,
            topEnd = OuterCornerSize,
            bottomEnd = OuterCornerSize
        )
    private val TrailingCheckedShape = ShapeTokens.CornerFull

    /**
     * Default shapes for the leading button. This defines the shapes the leading button should
     * morph to when enabled, pressed etc.
     *
     * @param endCornerSize the size for top end corner and bottom end corner
     */
    fun leadingButtonShapes(endCornerSize: CornerSize = InnerCornerSize) =
        SplitButtonShapes(
            shape = leadingButtonShape(endCornerSize),
            pressedShape = LeadingPressedShape,
            checkedShape = null,
        )

    /** Default shape of the trailing button */
    private fun trailingButtonShape(startCornerSize: CornerSize = InnerCornerSize) =
        RoundedCornerShape(startCornerSize, OuterCornerSize, OuterCornerSize, startCornerSize)

    /**
     * Default shapes for the trailing button
     *
     * @param startCornerSize the size for top start corner and bottom start corner
     */
    fun trailingButtonShapes(startCornerSize: CornerSize = InnerCornerSize) =
        SplitButtonShapes(
            shape = trailingButtonShape(startCornerSize),
            pressedShape = TrailingPressedShape,
            checkedShape = TrailingCheckedShape
        )

    /**
     * Create a default `leading` button that has the same visual as a Filled[Button]. To create a
     * `tonal`, `outlined`, or `elevated` version, the default value of [Button] params can be
     * passed in. For example, [ElevatedButton].
     *
     * The default text style for internal [Text] components will be set to [Typography.labelLarge].
     *
     * @param onClick called when the button is clicked
     * @param modifier the [Modifier] to be applied to this button.
     * @param enabled controls the enabled state of the split button. When `false`, this component
     *   will
     * @param shapes defines the shapes of this button's container, border (when [border] is not
     *   null), and shadow (when using [elevation])
     * @param colors [ButtonColors] that will be used to resolve the colors for this button in
     *   different states. See [ButtonDefaults.buttonColors].
     * @param elevation [ButtonElevation] used to resolve the elevation for this button in different
     *   states. This controls the size of the shadow below the button. See
     *   [ButtonElevation.shadowElevation].
     * @param border the border to draw around the container of this button contentPadding the
     *   spacing values to apply internally between the container and the content
     * @param contentPadding the spacing values to apply internally between the container and the
     *   content
     * @param interactionSource an optional hoisted [MutableInteractionSource] for observing and
     *   emitting [Interaction]s for this button. You can use this to change the button's appearance
     *   or preview the button in different states. Note that if `null` is provided, interactions
     *   will still happen internally.
     * @param content the content for the button.
     */
    @ExperimentalMaterial3ExpressiveApi
    @Composable
    fun LeadingButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        shapes: SplitButtonShapes = leadingButtonShapes(),
        colors: ButtonColors = ButtonDefaults.buttonColors(),
        elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
        border: BorderStroke? = null,
        contentPadding: PaddingValues = LeadingButtonContentPadding,
        interactionSource: MutableInteractionSource? = null,
        content: @Composable RowScope.() -> Unit
    ) {
        @Suppress("NAME_SHADOWING")
        val interactionSource = interactionSource ?: remember { MutableInteractionSource() }

        // TODO Load the motionScheme tokens from the component tokens file
        val defaultAnimationSpec = MotionSchemeKeyTokens.DefaultEffects.value<Float>()
        val pressed by interactionSource.collectIsPressedAsState()

        Surface(
            onClick = onClick,
            modifier = modifier.semantics { role = Role.Button },
            enabled = enabled,
            shape = shapeByInteraction(shapes, pressed, checked = false, defaultAnimationSpec),
            color = colors.containerColor,
            contentColor = colors.contentColor,
            shadowElevation = elevation?.shadowElevation(enabled, interactionSource)?.value ?: 0.dp,
            border = border,
            interactionSource = interactionSource
        ) {
            ProvideContentColorTextStyle(
                contentColor = colors.contentColor,
                textStyle = MaterialTheme.typography.labelLarge
            ) {
                Row(
                    Modifier.defaultMinSize(minWidth = LeadingButtonMinWidth, minHeight = MinHeight)
                        .padding(contentPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    }

    /**
     * Creates a `trailing` button that has the same visual as a Filled[Button]. When [checked] is
     * updated from `false` to `true`, the buttons corners will morph to `full` by default. Pressed
     * shape and checked shape can be customized via [shapes] param.
     *
     * To create a `tonal`, `outlined`, or `elevated` version, the default value of [Button] params
     * can be passed in. For example, [ElevatedButton].
     *
     * The default text style for internal [Text] components will be set to [Typography.labelLarge].
     *
     * @param onClick called when the button is clicked
     * @param checked indicates whether the button is toggled to a `checked` state. This will
     *   trigger the corner morphing animation to reflect the updated state.
     * @param modifier the [Modifier] to be applied to this button.
     * @param enabled controls the enabled state of the split button. When `false`, this component
     *   will
     * @param shapes the shapes for the container when transition between different
     *   [ShapeAnimationState]
     * @param colors [ButtonColors] that will be used to resolve the colors for this button in
     *   different states. See [ButtonDefaults.buttonColors].
     * @param elevation [ButtonElevation] used to resolve the elevation for this button in different
     *   states. This controls the size of the shadow below the button. See
     *   [ButtonElevation.shadowElevation].
     * @param border the border to draw around the container of this button contentPadding the
     *   spacing values to apply internally between the container and the content
     * @param contentPadding the spacing values to apply internally between the container and the
     *   content
     * @param interactionSource an optional hoisted [MutableInteractionSource] for observing and
     *   emitting [Interaction]s for this button. You can use this to change the button's appearance
     *   or preview the button in different states. Note that if `null` is provided, interactions
     *   will still happen internally.
     * @param content the content to be placed in the button
     */
    @Composable
    @ExperimentalMaterial3ExpressiveApi
    fun TrailingButton(
        onClick: () -> Unit,
        checked: Boolean,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        shapes: SplitButtonShapes = trailingButtonShapes(),
        colors: ButtonColors = ButtonDefaults.buttonColors(),
        elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
        border: BorderStroke? = null,
        contentPadding: PaddingValues = TrailingButtonContentPadding,
        interactionSource: MutableInteractionSource? = null,
        content: @Composable RowScope.() -> Unit
    ) {
        @Suppress("NAME_SHADOWING")
        val interactionSource = interactionSource ?: remember { MutableInteractionSource() }

        // TODO Load the motionScheme tokens from the component tokens file
        val defaultAnimationSpec = MotionSchemeKeyTokens.DefaultEffects.value<Float>()
        val pressed by interactionSource.collectIsPressedAsState()

        val density = LocalDensity.current
        val shape = shapeByInteraction(shapes, pressed, checked, defaultAnimationSpec)

        Surface(
            onClick = onClick,
            modifier =
                modifier
                    .drawWithContent {
                        drawContent()
                        if (checked) {
                            drawOutline(
                                outline = shape.createOutline(size, layoutDirection, density),
                                color = colors.contentColor,
                                alpha = TrailingButtonStateLayerAlpha
                            )
                        }
                    }
                    .semantics { role = Role.Button },
            enabled = enabled,
            shape = shape,
            color = colors.containerColor,
            contentColor = colors.contentColor,
            shadowElevation = elevation?.shadowElevation(enabled, interactionSource)?.value ?: 0.dp,
            border = border,
            interactionSource = interactionSource
        ) {
            ProvideContentColorTextStyle(
                contentColor = colors.contentColor,
                textStyle = MaterialTheme.typography.labelLarge
            ) {
                Row(
                    Modifier.defaultMinSize(
                            minWidth = TrailingButtonMinWidth,
                            minHeight = MinHeight
                        )
                        .then(
                            when (shape) {
                                is ShapeWithOpticalCentering -> {
                                    Modifier.opticalCentering(
                                        shape = shape,
                                        basePadding = contentPadding
                                    )
                                }
                                is CornerBasedShape -> {
                                    Modifier.opticalCentering(
                                        shape = shape,
                                        basePadding = contentPadding
                                    )
                                }
                                else -> {
                                    Modifier.padding(contentPadding)
                                }
                            }
                        ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TonalLeadingButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    shapes: SplitButtonShapes,
    content: @Composable RowScope.() -> Unit
) {
    SplitButtonDefaults.LeadingButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        shapes = shapes,
        colors = ButtonDefaults.filledTonalButtonColors(),
        elevation = ButtonDefaults.filledTonalButtonElevation(),
        border = null,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TonalTrailingButton(
    onClick: () -> Unit,
    checked: Boolean,
    modifier: Modifier,
    enabled: Boolean,
    shapes: SplitButtonShapes,
    content: @Composable RowScope.() -> Unit
) {
    SplitButtonDefaults.TrailingButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        shapes = shapes,
        checked = checked,
        colors = ButtonDefaults.filledTonalButtonColors(),
        elevation = ButtonDefaults.filledTonalButtonElevation(),
        border = null,
        content = content
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun OutlinedLeadingButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    shapes: SplitButtonShapes,
    content: @Composable RowScope.() -> Unit
) {
    SplitButtonDefaults.LeadingButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        shapes = shapes,
        colors = ButtonDefaults.outlinedButtonColors(),
        elevation = null,
        border = ButtonDefaults.outlinedButtonBorder(enabled),
        content = content
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun OutlinedTrailingButton(
    onClick: () -> Unit,
    checked: Boolean,
    modifier: Modifier,
    enabled: Boolean,
    shapes: SplitButtonShapes,
    content: @Composable RowScope.() -> Unit
) {
    SplitButtonDefaults.TrailingButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        shapes = shapes,
        checked = checked,
        colors = ButtonDefaults.outlinedButtonColors(),
        elevation = null,
        border = ButtonDefaults.outlinedButtonBorder(enabled),
        content = content
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ElevatedLeadingButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    shapes: SplitButtonShapes,
    content: @Composable RowScope.() -> Unit
) {
    SplitButtonDefaults.LeadingButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        shapes = shapes,
        colors = ButtonDefaults.elevatedButtonColors(),
        elevation = ButtonDefaults.elevatedButtonElevation(),
        border = null,
        content = content
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ElevatedTrailingButton(
    onClick: () -> Unit,
    checked: Boolean,
    modifier: Modifier,
    enabled: Boolean,
    shapes: SplitButtonShapes,
    content: @Composable RowScope.() -> Unit
) {
    SplitButtonDefaults.TrailingButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        shapes = shapes,
        checked = checked,
        colors = ButtonDefaults.elevatedButtonColors(),
        elevation = ButtonDefaults.elevatedButtonElevation(),
        border = null,
        content = content
    )
}

@Composable
private fun shapeByInteraction(
    shapes: SplitButtonShapes,
    pressed: Boolean,
    checked: Boolean,
    animationSpec: FiniteAnimationSpec<Float>
): Shape {
    if (shapes.hasRoundedCornerShapes) {
        return rememberAnimatedShape(shapes, pressed, checked, animationSpec)
    }
    if (pressed) {
        return shapes.pressedShape ?: shapes.shape
    }
    if (checked) {
        return shapes.checkedShape ?: shapes.shape
    }
    return shapes.shape
}

@Composable
private fun rememberAnimatedShape(
    shapes: SplitButtonShapes,
    pressed: Boolean,
    checked: Boolean,
    animationSpec: FiniteAnimationSpec<Float>,
): Shape {
    val defaultShape = shapes.shape as RoundedCornerShape
    val pressedShape = shapes.pressedShape as RoundedCornerShape?
    val checkedShape = shapes.checkedShape as RoundedCornerShape?
    val currentShape =
        if (pressedShape != null && pressed) pressedShape
        else if (checkedShape != null && checked) checkedShape else defaultShape

    val state =
        remember(animationSpec) {
            ShapeAnimationState(
                shape = currentShape,
                spec = animationSpec,
            )
        }

    val channel = remember { Channel<RoundedCornerShape>(Channel.CONFLATED) }
    SideEffect { channel.trySend(currentShape) }
    LaunchedEffect(state, channel) {
        for (target in channel) {
            val newTarget = channel.tryReceive().getOrNull() ?: target
            launch { state.animateToShape(newTarget) }
        }
    }

    return rememberAnimatedShape(state)
}

@Composable
private fun rememberAnimatedShape(state: ShapeAnimationState): Shape {
    val density = LocalDensity.current
    state.density = density

    return remember(density, state) {
        object : ShapeWithOpticalCentering {
            var clampedRange by mutableStateOf(0f..1f)

            override fun offset(): Float {
                val topStart = state.topStart?.value?.coerceIn(clampedRange) ?: 0f
                val topEnd = state.topEnd?.value?.coerceIn(clampedRange) ?: 0f
                val bottomStart = state.bottomStart?.value?.coerceIn(clampedRange) ?: 0f
                val bottomEnd = state.bottomEnd?.value?.coerceIn(clampedRange) ?: 0f
                val avgStart = (topStart + bottomStart) / 2
                val avgEnd = (topEnd + bottomEnd) / 2
                return OpticalCenteringCoefficient * (avgStart - avgEnd)
            }

            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                state.size = size
                if (!state.didInit) {
                    state.init()
                }

                clampedRange = 0f..size.height / 2
                return RoundedCornerShape(
                        topStart = state.topStart?.value?.coerceIn(clampedRange) ?: 0f,
                        topEnd = state.topEnd?.value?.coerceIn(clampedRange) ?: 0f,
                        bottomStart = state.bottomStart?.value?.coerceIn(clampedRange) ?: 0f,
                        bottomEnd = state.bottomEnd?.value?.coerceIn(clampedRange) ?: 0f,
                    )
                    .createOutline(size, layoutDirection, density)
            }
        }
    }
}

/**
 * The shapes that will be used in [SplitButton]. Split button will morph between these shapes
 * depending on the interaction of the buttons, assuming all of the shapes are [CornerBasedShape]s.
 *
 * @property shape is the default shape.
 * @property pressedShape is the pressed shape.
 * @property checkedShape is the checked shape.
 */
data class SplitButtonShapes(val shape: Shape, val pressedShape: Shape?, val checkedShape: Shape?)

internal val SplitButtonShapes.hasRoundedCornerShapes: Boolean
    get() {
        // Ignore null shapes and only check default shape for RoundedCorner
        if (pressedShape != null && pressedShape !is RoundedCornerShape) return false
        if (checkedShape != null && checkedShape !is RoundedCornerShape) return false
        return shape is RoundedCornerShape
    }

@Stable
private class ShapeAnimationState(
    val shape: RoundedCornerShape,
    val spec: FiniteAnimationSpec<Float>,
) {
    var size: Size = Size.Zero
    var density: Density = Density(0f, 0f)
    var didInit = false

    var topStart: Animatable<Float, AnimationVector1D>? = null
        private set

    var topEnd: Animatable<Float, AnimationVector1D>? = null
        private set

    var bottomStart: Animatable<Float, AnimationVector1D>? = null
        private set

    var bottomEnd: Animatable<Float, AnimationVector1D>? = null
        private set

    fun init() {
        topStart = Animatable(shape.topStart.toPx(size, density))
        topEnd = Animatable(shape.topEnd.toPx(size, density))
        bottomStart = Animatable(shape.bottomStart.toPx(size, density))
        bottomEnd = Animatable(shape.bottomEnd.toPx(size, density))
        didInit = true
    }

    suspend fun animateToShape(shape: RoundedCornerShape?) =
        shape?.let {
            coroutineScope {
                launch { topStart?.animateTo(it.topStart.toPx(size, density), spec) }
                launch { topEnd?.animateTo(it.topEnd.toPx(size, density), spec) }
                launch { bottomStart?.animateTo(it.bottomStart.toPx(size, density), spec) }
                launch { bottomEnd?.animateTo(it.bottomEnd.toPx(size, density), spec) }
            }
        }
}

private const val LeadingButtonLayoutId = "LeadingButton"
private const val TrailingButtonLayoutId = "TrailingButton"
