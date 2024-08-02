/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package ru.tech.imageresizershrinker.color_tools.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Blender
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttoolfactory.colordetector.parser.rememberColorParser
import com.smarttoolfactory.colordetector.util.ColorUtil.colorToHex
import com.smarttoolfactory.colordetector.util.ColorUtil.colorToHexAlpha
import com.t8rin.dynamic.theme.LocalDynamicThemeState
import com.t8rin.dynamic.theme.rememberAppColorTuple
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.color_tools.presentation.components.HarmonyType
import ru.tech.imageresizershrinker.color_tools.presentation.components.applyHarmony
import ru.tech.imageresizershrinker.color_tools.presentation.components.icon
import ru.tech.imageresizershrinker.color_tools.presentation.components.mixWith
import ru.tech.imageresizershrinker.color_tools.presentation.components.title
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.Swatch
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.ui.theme.inverse
import ru.tech.imageresizershrinker.core.ui.utils.helper.ContextUtils.copyToClipboard
import ru.tech.imageresizershrinker.core.ui.utils.helper.isPortraitOrientationAsState
import ru.tech.imageresizershrinker.core.ui.utils.helper.toHex
import ru.tech.imageresizershrinker.core.ui.widget.AdaptiveLayoutScreen
import ru.tech.imageresizershrinker.core.ui.widget.buttons.EnhancedChip
import ru.tech.imageresizershrinker.core.ui.widget.controls.EnhancedSliderItem
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.BackgroundColorSelector
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.modifier.transparencyChecker
import ru.tech.imageresizershrinker.core.ui.widget.other.ExpandableItem
import ru.tech.imageresizershrinker.core.ui.widget.other.LocalToastHostState
import ru.tech.imageresizershrinker.core.ui.widget.other.TopAppBarEmoji
import ru.tech.imageresizershrinker.core.ui.widget.saver.ColorSaver
import ru.tech.imageresizershrinker.core.ui.widget.text.TitleItem
import ru.tech.imageresizershrinker.core.ui.widget.text.marquee
import kotlin.math.roundToInt

@Composable
fun ColorToolsContent(
    onGoBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val toastHostState = LocalToastHostState.current
    val context = LocalContext.current
    val themeState = LocalDynamicThemeState.current
    val settingsState = LocalSettingsState.current
    val allowChangeColor = settingsState.allowChangeColorByImage

    val appColorTuple = rememberAppColorTuple(
        defaultColorTuple = settingsState.appColorTuple,
        dynamicColor = settingsState.isDynamicColors,
        darkTheme = settingsState.isNightMode
    )

    var selectedColor by rememberSaveable(
        stateSaver = ColorSaver
    ) {
        mutableStateOf(appColorTuple.primary)
    }

    LaunchedEffect(selectedColor) {
        if (allowChangeColor) {
            themeState.updateColor(selectedColor)
        }
    }

    val isPortrait by isPortraitOrientationAsState()

    AdaptiveLayoutScreen(
        title = {
            Text(
                text = stringResource(R.string.color_tools),
                textAlign = TextAlign.Center,
                modifier = Modifier.marquee()
            )
        },
        onGoBack = onGoBack,
        actions = {},
        topAppBarPersistentActions = {
            TopAppBarEmoji()
        },
        imagePreview = {},
        controls = {
            var selectedHarmony by rememberSaveable {
                mutableStateOf(HarmonyType.COMPLEMENTARY)
            }
            val harmonies by remember(selectedColor, selectedHarmony) {
                derivedStateOf {
                    selectedColor.applyHarmony(selectedHarmony)
                }
            }
            var shadingVariation by rememberSaveable {
                mutableIntStateOf(5)
            }
            val shades by remember(selectedColor, shadingVariation) {
                derivedStateOf {
                    selectedColor.mixWith(
                        color = Color.Black,
                        variations = shadingVariation,
                        maxPercent = 0.9f
                    )
                }
            }
            val tones by remember(selectedColor, shadingVariation) {
                derivedStateOf {
                    selectedColor.mixWith(
                        color = Color(0xff8e918f),
                        variations = shadingVariation,
                        maxPercent = 0.9f
                    )
                }
            }
            val tints by remember(selectedColor, shadingVariation) {
                derivedStateOf {
                    selectedColor.mixWith(
                        color = Color.White,
                        variations = shadingVariation,
                        maxPercent = 0.8f
                    )
                }
            }
            var mixingVariation by rememberSaveable {
                mutableIntStateOf(5)
            }
            var colorToMix by rememberSaveable(
                stateSaver = ColorSaver
            ) {
                mutableStateOf(appColorTuple.tertiary ?: Color.Yellow)
            }
            val mixedColors by remember(selectedColor, mixingVariation, colorToMix) {
                derivedStateOf {
                    selectedColor.mixWith(
                        color = colorToMix,
                        variations = mixingVariation,
                        maxPercent = 1f
                    )
                }
            }
            if (isPortrait) {
                Spacer(modifier = Modifier.height(20.dp))
            }
            BackgroundColorSelector(
                value = selectedColor,
                onValueChange = { selectedColor = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .container(
                        shape = RoundedCornerShape(20.dp)
                    ),
                title = stringResource(R.string.first_color)
            )
            Spacer(modifier = Modifier.height(16.dp))
            ExpandableItem(
                visibleContent = {
                    TitleItem(
                        text = stringResource(R.string.color_mixing),
                        icon = Icons.Rounded.Blender
                    )
                },
                expandableContent = {
                    Column(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 8.dp
                        ),
                    ) {
                        BackgroundColorSelector(
                            value = colorToMix,
                            onValueChange = { colorToMix = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .container(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = ContainerShapeDefaults.topShape
                                ),
                            title = stringResource(R.string.second_color)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        EnhancedSliderItem(
                            value = mixingVariation,
                            title = stringResource(R.string.variation),
                            valueRange = 2f..15f,
                            onValueChange = { mixingVariation = it.roundToInt() },
                            internalStateTransformation = { it.roundToInt() },
                            shape = ContainerShapeDefaults.bottomShape,
                            behaveAsContainer = true,
                            color = MaterialTheme.colorScheme.surface,
                            steps = 12
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            mixedColors.forEachIndexed { index, color ->
                                val boxColor by animateColorAsState(color)
                                val contentColor = boxColor.inverse(
                                    fraction = { cond ->
                                        if (cond) 0.8f
                                        else 0.5f
                                    },
                                    darkMode = boxColor.luminance() < 0.3f
                                )
                                Box(
                                    modifier = Modifier
                                        .heightIn(min = 100.dp)
                                        .fillMaxWidth()
                                        .clip(
                                            ContainerShapeDefaults.shapeForIndex(
                                                index = index,
                                                size = mixedColors.size
                                            )
                                        )
                                        .transparencyChecker()
                                        .background(boxColor)
                                        .clickable {
                                            context.copyToClipboard(
                                                label = context.getString(R.string.color),
                                                value = getFormattedColor(color)
                                            )
                                            scope.launch {
                                                toastHostState.showToast(
                                                    icon = Icons.Rounded.ContentPaste,
                                                    message = context.getString(R.string.color_copied)
                                                )
                                            }
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = stringResource(R.string.edit),
                                        tint = contentColor,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(28.dp)
                                            .background(
                                                color = boxColor.copy(alpha = 1f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(2.dp)
                                    )

                                    Text(
                                        text = color.toHex(),
                                        color = contentColor,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(4.dp)
                                            .background(
                                                color = boxColor.copy(alpha = 1f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 4.dp),
                                        fontSize = 12.sp
                                    )

                                    val parser = rememberColorParser()
                                    Text(
                                        text = remember(color) {
                                            derivedStateOf {
                                                parser.parseColorName(color)
                                            }
                                        }.value,
                                        color = contentColor,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                            .background(
                                                color = boxColor.copy(alpha = 1f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 4.dp),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                },
                initialState = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExpandableItem(
                visibleContent = {
                    TitleItem(
                        text = stringResource(R.string.color_harmonies),
                        icon = Icons.Rounded.BarChart
                    )
                },
                expandableContent = {
                    Column(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 8.dp
                        ),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HarmonyType.entries.forEach {
                                EnhancedChip(
                                    selected = it == selectedHarmony,
                                    onClick = { selectedHarmony = it },
                                    selectedColor = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = it.icon(),
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        AnimatedContent(
                            targetState = selectedHarmony,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            }
                        ) {
                            Text(it.title())
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            harmonies.forEachIndexed { index, color ->
                                val boxColor by animateColorAsState(color)
                                val contentColor = boxColor.inverse(
                                    fraction = { cond ->
                                        if (cond) 0.8f
                                        else 0.5f
                                    },
                                    darkMode = boxColor.luminance() < 0.3f
                                )
                                Box(
                                    modifier = Modifier
                                        .heightIn(min = 120.dp)
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .transparencyChecker()
                                        .background(boxColor)
                                        .clickable {
                                            context.copyToClipboard(
                                                label = context.getString(R.string.color),
                                                value = getFormattedColor(color)
                                            )
                                            scope.launch {
                                                toastHostState.showToast(
                                                    icon = Icons.Rounded.ContentPaste,
                                                    message = context.getString(R.string.color_copied)
                                                )
                                            }
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = stringResource(R.string.edit),
                                        tint = contentColor,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(28.dp)
                                            .background(
                                                color = boxColor.copy(alpha = 1f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(2.dp)
                                    )

                                    Text(
                                        text = color.toHex(),
                                        color = contentColor,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(4.dp)
                                            .background(
                                                color = boxColor.copy(alpha = 1f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 4.dp),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                },
                initialState = false
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExpandableItem(
                visibleContent = {
                    TitleItem(
                        text = stringResource(R.string.color_shading),
                        icon = Icons.Rounded.Swatch
                    )
                },
                expandableContent = {
                    Column(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 8.dp
                        ),
                    ) {
                        EnhancedSliderItem(
                            value = shadingVariation,
                            title = stringResource(R.string.variation),
                            valueRange = 2f..15f,
                            onValueChange = { shadingVariation = it.roundToInt() },
                            internalStateTransformation = { it.roundToInt() },
                            behaveAsContainer = true,
                            color = MaterialTheme.colorScheme.surface,
                            steps = 12
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                tints to R.string.tints,
                                tones to R.string.tones,
                                shades to R.string.shades
                            ).forEach { (data, title) ->
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = stringResource(title))
                                    data.forEachIndexed { index, color ->
                                        val boxColor by animateColorAsState(color)
                                        val contentColor = boxColor.inverse(
                                            fraction = { cond ->
                                                if (cond) 0.8f
                                                else 0.5f
                                            },
                                            darkMode = boxColor.luminance() < 0.3f
                                        )
                                        Box(
                                            modifier = Modifier
                                                .heightIn(min = 100.dp)
                                                .fillMaxWidth()
                                                .clip(
                                                    ContainerShapeDefaults.shapeForIndex(
                                                        index = index,
                                                        size = data.size
                                                    )
                                                )
                                                .transparencyChecker()
                                                .background(boxColor)
                                                .clickable {
                                                    context.copyToClipboard(
                                                        label = context.getString(R.string.color),
                                                        value = getFormattedColor(color)
                                                    )
                                                    scope.launch {
                                                        toastHostState.showToast(
                                                            icon = Icons.Rounded.ContentPaste,
                                                            message = context.getString(R.string.color_copied)
                                                        )
                                                    }
                                                }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.ContentCopy,
                                                contentDescription = stringResource(R.string.edit),
                                                tint = contentColor,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(28.dp)
                                                    .background(
                                                        color = boxColor.copy(alpha = 1f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(2.dp)
                                            )

                                            Text(
                                                text = color.toHex(),
                                                color = contentColor,
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(4.dp)
                                                    .background(
                                                        color = boxColor.copy(alpha = 1f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 4.dp),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                initialState = false
            )
        },
        buttons = {},
        placeImagePreview = false,
        canShowScreenData = true,
        isPortrait = isPortrait
    )
}

private fun getFormattedColor(color: Color): String {
    return if (color.alpha == 1f) {
        colorToHex(color)
    } else {
        colorToHexAlpha(color)
    }.uppercase()
}