/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.exercisesamplecompose.presentation.ambient

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.withSaveLayer
import com.google.android.horologist.compose.ambient.AmbientState

/**
 * A Paint object configured to apply a grayscale effect.
 *
 * This is achieved by using a ColorMatrix to set the saturation to 0,
 * effectively removing all color information from the image.
 * Anti-aliasing is disabled for this paint to potentially improve performance.
 */
private val grayscale =
    Paint().apply {
        colorFilter =
            ColorFilter.colorMatrix(
                ColorMatrix().apply {
                    setToSaturation(0f)
                }
            )
        isAntiAlias = false
    }

/**
 * Applies a grayscale effect and scales down the content when in ambient mode.
 *
 * This modifier checks the provided [AmbientState] to determine if the device is
 * in ambient mode. If it is, the content is scaled down by 10% and a grayscale
 * filter is applied. When not in ambient mode, the content is rendered normally.
 */
fun Modifier.ambientGray(ambientState: AmbientState): Modifier =
    graphicsLayer {
        if (ambientState.isAmbient) {
            scaleX = 0.9f
            scaleY = 0.9f
        }
    }.drawWithContent {
        if (ambientState.isAmbient) {
            drawIntoCanvas {
                it.withSaveLayer(size.toRect(), grayscale) {
                    drawContent()
                }
            }
        } else {
            drawContent()
        }
    }

/**
 * This modifier conditionally draws the content based on the state provided by an [AmbientState].
 *
 * If the `isInteractive` property of the provided [ambientState] is true, the content will be drawn.
 * Otherwise, the content will not be drawn, effectively leaving the area blank.
 */
fun Modifier.ambientBlank(ambientState: AmbientState): Modifier =
    drawWithContent {
        if (ambientState.isInteractive) {
            drawContent()
        }
    }
