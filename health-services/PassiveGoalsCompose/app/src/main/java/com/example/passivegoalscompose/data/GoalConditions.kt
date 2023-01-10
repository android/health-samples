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
import androidx.health.services.client.data.ComparisonType
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeCondition
import androidx.health.services.client.data.PassiveGoal

val dailyStepsGoal by lazy {
    val condition = DataTypeCondition(
        dataType = DataType.STEPS_DAILY,
        threshold = 10_000, // trigger every "threshold" steps
        comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
    )
    PassiveGoal(condition)
}

val floorsGoal by lazy {
    val condition = DataTypeCondition(
        dataType = DataType.FLOORS_DAILY,
        threshold = 3.0, // trigger every "threshold" floors climbed
        comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
    )
    PassiveGoal(condition)
}
