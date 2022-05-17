/*
 * Copyright 2022 The Android Open Source Project
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
package com.example.healthconnectsample.presentation.navigation

import com.example.healthconnectsample.R

const val UID_NAV_ARGUMENT = "uid"

/**
 * Represent all Screens in the app.
 *
 * @param route The route string used for Compose navigation
 * @param titleId The ID of the string resource to display as a title
 * @param hasMenuItem Whether this Screen should be shown as a menu item in the left-hand menu (not
 *     all screens in the navigation graph are intended to be directly reached from the menu).
 */
enum class Screen(val route: String, val titleId: Int, val hasMenuItem: Boolean = true) {
    WelcomeScreen("welcome_screen", R.string.welcome_screen, false),
    ActivitySessions("activity_sessions", R.string.activity_sessions),
    ActivitySessionDetail("activity_session_detail", R.string.activity_session_detail, false),
    SleepSessions("sleep_sessions", R.string.sleep_sessions),
    SleepSessionDetail("sleep_session_detail", R.string.sleep_session_detail, false),
    InputReadings("input_readings", R.string.input_readings),
    PrivacyPolicy("privacy_policy", R.string.sleep_session_detail, false)
}
