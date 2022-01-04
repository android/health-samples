/*
 * Copyright 2021 The Android Open Source Project
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
package com.example.healthplatformsample.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.example.healthplatformsample.R
import com.example.healthplatformsample.presentation.theme.HealthPlatformSampleTheme

@Composable
fun HealthPlatformNotSupported(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val tag = stringResource(R.string.not_supported_tag)
        val url = stringResource(R.string.not_supported_url)
        val handler = LocalUriHandler.current

        val unavailableText = buildAnnotatedString {
            append(stringResource(id = R.string.not_supported))
            append(" ")

            pushStringAnnotation(tag = tag, annotation = url)
            withStyle(style = SpanStyle(color = MaterialTheme.colors.primary)) {
                append(stringResource(R.string.not_supported_further_details))
            }
        }
        ClickableText(
            text = unavailableText
        ) { offset ->
            unavailableText.getStringAnnotations(tag = tag, start = offset, end = offset)
                .firstOrNull()?.let {
                    handler.openUri(it.item)
                }
        }
    }
}

@Preview
@Composable
fun HealthPlatformNotSupportedPreview() {
    HealthPlatformSampleTheme {
        HealthPlatformNotSupported()
    }
}
