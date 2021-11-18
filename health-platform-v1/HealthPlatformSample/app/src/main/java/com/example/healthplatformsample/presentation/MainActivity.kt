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
package com.example.healthplatformsample.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.healthplatformsample.R
import com.example.healthplatformsample.data.HealthPlatformManager
import com.example.healthplatformsample.presentation.components.HealthPlatformBottomNavigation
import com.example.healthplatformsample.presentation.components.HealthPlatformNotSupported
import com.example.healthplatformsample.presentation.navigation.HealthPlatformNavigation
import com.example.healthplatformsample.presentation.theme.HealthPlatformSampleTheme

/**
 * The entry point into the sample.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val healthPlatformManager = (application as BaseApplication).healthPlatformManager

        setContent {
            HealthPlatformApp(healthPlatformManager = healthPlatformManager)
        }
    }
}

@Composable
fun HealthPlatformApp(healthPlatformManager: HealthPlatformManager) {
    HealthPlatformSampleTheme {
        val scaffoldState = rememberScaffoldState()
        val navController = rememberNavController()
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) }
                )
            },
            bottomBar = { HealthPlatformBottomNavigation(navController = navController) },
            snackbarHost = {
                SnackbarHost(it) { data -> Snackbar(snackbarData = data) }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, innerPadding.calculateBottomPadding())
            ) {
                if (healthPlatformManager.healthPlatformSupported()) {
                    HealthPlatformNavigation(
                        navController = navController,
                        healthPlatformManager = healthPlatformManager,
                        scaffoldState = scaffoldState
                    )
                } else {
                    HealthPlatformNotSupported()
                }
            }
        }
    }
}
