/*
 * Copyright (C) 2022 The Android Open Source Project
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
package androidx.health.connect.client.permission

import androidx.annotation.RestrictTo
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalBodyTemperatureRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.BoneMassRecord
import androidx.health.connect.client.records.CervicalMucusRecord
import androidx.health.connect.client.records.CyclingPedalingCadenceRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ElevationGainedRecord
import androidx.health.connect.client.records.ExerciseEventRecord
import androidx.health.connect.client.records.ExerciseLapRecord
import androidx.health.connect.client.records.ExerciseRepetitionsRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityDifferentialIndexRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.HeartRateVariabilitySRecord
import androidx.health.connect.client.records.HeartRateVariabilitySd2Record
import androidx.health.connect.client.records.HeartRateVariabilitySdannRecord
import androidx.health.connect.client.records.HeartRateVariabilitySdnnIndexRecord
import androidx.health.connect.client.records.HeartRateVariabilitySdnnRecord
import androidx.health.connect.client.records.HeartRateVariabilitySdsdRecord
import androidx.health.connect.client.records.HeartRateVariabilityTinnRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.HipCircumferenceRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.MenstruationFlowRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.OvulationTestRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.PowerRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SexualActivityRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsCadenceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.SwimmingStrokesRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WaistCircumferenceRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.WheelchairPushesRecord
import kotlin.reflect.KClass

/**
 * A Permission either to read or write data associated with a [Record] type.
 *
 * @see androidx.health.connect.client.PermissionController
 */
public class HealthPermission
internal constructor(
    /** type of [Record] the permission gives access for. */
    internal val recordType: KClass<out Record>,
    /** whether read or write access. */
    @property:AccessType internal val accessType: Int,
) {
    companion object {
        /**
         * Creates [HealthPermission] to read provided [recordType], such as `Steps::class`.
         *
         * @return Permission object to use with
         * [androidx.health.connect.client.PermissionController].
         */
        @JvmStatic
        public fun createReadPermission(recordType: KClass<out Record>): HealthPermission {
            return HealthPermission(recordType, AccessTypes.READ)
        }

        /**
         * Returns a permission defined in [HealthPermission] to read provided [recordType], such as
         * `Steps::class`.
         *
         * @return Permission to use with [androidx.health.connect.client.PermissionController].
         * @throws IllegalArgumentException if the given record type is invalid.
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY) // Not yet ready for public
        @JvmStatic
        public fun createReadPermissionInternal(recordType: KClass<out Record>): String {
            if (RECORD_TYPE_TO_PERMISSION[recordType] == null) {
                throw IllegalArgumentException(
                    "Given recordType is not valid : $recordType.simpleName"
                )
            }
            return READ_PERMISSION_PREFIX + RECORD_TYPE_TO_PERMISSION[recordType]
        }

        /**
         * Creates [HealthPermission] to write provided [recordType], such as `Steps::class`.
         *
         * @return Permission to use with [androidx.health.connect.client.PermissionController].
         */
        @JvmStatic
        public fun createWritePermission(recordType: KClass<out Record>): HealthPermission {
            return HealthPermission(recordType, AccessTypes.WRITE)
        }

        /**
         * Returns a permission defined in [HealthPermission] to read provided [recordType], such as
         * `Steps::class`.
         *
         * @return Permission object to use with
         * [androidx.health.connect.client.PermissionController].
         * @throws IllegalArgumentException if the given record type is invalid.
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY) // Not yet ready for public
        @JvmStatic
        public fun createWritePermissionInternal(recordType: KClass<out Record>): String {
            if (RECORD_TYPE_TO_PERMISSION[recordType] == null) {
                throw IllegalArgumentException(
                    "Given recordType is not valid : $recordType.simpleName"
                )
            }
            return WRITE_PERMISSION_PREFIX + RECORD_TYPE_TO_PERMISSION.getOrDefault(recordType, "")
        }

        // Read permissions for ACTIVITY.
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_ACTIVE_CALORIES_BURNED =
            "android.permission.health.READ_ACTIVE_CALORIES_BURNED"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_DISTANCE = "android.permission.health.READ_DISTANCE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_ELEVATION_GAINED = "android.permission.health.READ_ELEVATION_GAINED"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_EXERCISE = "android.permission.health.READ_EXERCISE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_FLOORS_CLIMBED = "android.permission.health.READ_FLOORS_CLIMBED"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_STEPS = "android.permission.health.READ_STEPS"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_TOTAL_CALORIES_BURNED =
            "android.permission.health.READ_TOTAL_CALORIES_BURNED"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_VO2_MAX = "android.permission.health.READ_VO2_MAX"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_WHEELCHAIR_PUSHES = "android.permission.health.READ_WHEELCHAIR_PUSHES"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_POWER = "android.permission.health.READ_POWER"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_SPEED = "android.permission.health.READ_SPEED"

        // Read permissions for BODY_MEASUREMENTS.
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_BASAL_METABOLIC_RATE = "android.permission.health.READ_BASAL_METABOLIC_RATE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_BODY_FAT = "android.permission.health.READ_BODY_FAT"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_BODY_WATER_MASS = "android.permission.health.READ_BODY_WATER_MASS"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_BONE_MASS = "android.permission.health.READ_BONE_MASS"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_HEIGHT = "android.permission.health.READ_HEIGHT"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_HIP_CIRCUMFERENCE = "android.permission.health.READ_HIP_CIRCUMFERENCE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_LEAN_BODY_MASS = "android.permission.health.READ_LEAN_BODY_MASS"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_WAIST_CIRCUMFERENCE = "android.permission.health.READ_WAIST_CIRCUMFERENCE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_WEIGHT = "android.permission.health.READ_WEIGHT"

        // Read permissions for CYCLE_TRACKING.
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_CERVICAL_MUCUS = "android.permission.health.READ_CERVICAL_MUCUS"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_MENSTRUATION = "android.permission.health.READ_MENSTRUATION"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_OVULATION_TEST = "android.permission.health.READ_OVULATION_TEST"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_SEXUAL_ACTIVITY = "android.permission.health.READ_SEXUAL_ACTIVITY"

        // Read permissions for NUTRITION.
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_HYDRATION = "android.permission.health.READ_HYDRATION"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_NUTRITION = "android.permission.health.READ_NUTRITION"

        // Read permissions for SLEEP.
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_SLEEP = "android.permission.health.READ_SLEEP"

        // Read permissions for VITALS.
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_BASAL_BODY_TEMPERATURE =
            "android.permission.health.READ_BASAL_BODY_TEMPERATURE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_BLOOD_GLUCOSE = "android.permission.health.READ_BLOOD_GLUCOSE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_BLOOD_PRESSURE = "android.permission.health.READ_BLOOD_PRESSURE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_BODY_TEMPERATURE = "android.permission.health.READ_BODY_TEMPERATURE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_HEART_RATE = "android.permission.health.READ_HEART_RATE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_HEART_RATE_VARIABILITY =
            "android.permission.health.READ_HEART_RATE_VARIABILITY"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_INTERMENSTRUAL_BLEEDING =
            "android.permission.health.READ_INTERMENSTRUAL_BLEEDING"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_OXYGEN_SATURATION = "android.permission.health.READ_OXYGEN_SATURATION"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_RESPIRATORY_RATE = "android.permission.health.READ_RESPIRATORY_RATE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val READ_RESTING_HEART_RATE = "android.permission.health.READ_RESTING_HEART_RATE"

        // Write permissions for ACTIVITY.
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_ACTIVE_CALORIES_BURNED =
            "android.permission.health.WRITE_ACTIVE_CALORIES_BURNED"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_DISTANCE = "android.permission.health.WRITE_DISTANCE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_ELEVATION_GAINED = "android.permission.health.WRITE_ELEVATION_GAINED"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_EXERCISE = "android.permission.health.WRITE_EXERCISE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_FLOORS_CLIMBED = "android.permission.health.WRITE_FLOORS_CLIMBED"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_STEPS = "android.permission.health.WRITE_STEPS"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_TOTAL_CALORIES_BURNED =
            "android.permission.health.WRITE_TOTAL_CALORIES_BURNED"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_VO2_MAX = "android.permission.health.WRITE_VO2_MAX"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_WHEELCHAIR_PUSHES = "android.permission.health.WRITE_WHEELCHAIR_PUSHES"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_POWER = "android.permission.health.WRITE_POWER"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_SPEED = "android.permission.health.WRITE_SPEED"

        // Write permissions for BODY_MEASUREMENTS.
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_BASAL_METABOLIC_RATE =
            "android.permission.health.WRITE_BASAL_METABOLIC_RATE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_BODY_FAT = "android.permission.health.WRITE_BODY_FAT"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_BODY_WATER_MASS = "android.permission.health.WRITE_BODY_WATER_MASS"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_BONE_MASS = "android.permission.health.WRITE_BONE_MASS"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_HEIGHT = "android.permission.health.WRITE_HEIGHT"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_HIP_CIRCUMFERENCE = "android.permission.health.WRITE_HIP_CIRCUMFERENCE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_INTERMENSTRUAL_BLEEDING =
            "android.permission.health.WRITE_INTERMENSTRUAL_BLEEDING"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_LEAN_BODY_MASS = "android.permission.health.WRITE_LEAN_BODY_MASS"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_WAIST_CIRCUMFERENCE = "android.permission.health.WRITE_WAIST_CIRCUMFERENCE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_WEIGHT = "android.permission.health.WRITE_WEIGHT"

        // Write permissions for CYCLE_TRACKING.
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_CERVICAL_MUCUS = "android.permission.health.WRITE_CERVICAL_MUCUS"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_MENSTRUATION = "android.permission.health.WRITE_MENSTRUATION"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_OVULATION_TEST = "android.permission.health.WRITE_OVULATION_TEST"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_SEXUAL_ACTIVITY = "android.permission.health.WRITE_SEXUAL_ACTIVITY"

        // Write permissions for NUTRITION.
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_HYDRATION = "android.permission.health.WRITE_HYDRATION"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_NUTRITION = "android.permission.health.WRITE_NUTRITION"

        // Write permissions for SLEEP.
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_SLEEP = "android.permission.health.WRITE_SLEEP"

        // Write permissions for VITALS.
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_BASAL_BODY_TEMPERATURE =
            "android.permission.health.WRITE_BASAL_BODY_TEMPERATURE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_BLOOD_GLUCOSE = "android.permission.health.WRITE_BLOOD_GLUCOSE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_BLOOD_PRESSURE = "android.permission.health.WRITE_BLOOD_PRESSURE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_BODY_TEMPERATURE = "android.permission.health.WRITE_BODY_TEMPERATURE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_HEART_RATE = "android.permission.health.WRITE_HEART_RATE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_HEART_RATE_VARIABILITY =
            "android.permission.health.WRITE_HEART_RATE_VARIABILITY"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_OXYGEN_SATURATION = "android.permission.health.WRITE_OXYGEN_SATURATION"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_RESPIRATORY_RATE = "android.permission.health.WRITE_RESPIRATORY_RATE"
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        const val WRITE_RESTING_HEART_RATE = "android.permission.health.WRITE_RESTING_HEART_RATE"

        private const val READ_PERMISSION_PREFIX = "android.permission.health.READ_"

        private const val WRITE_PERMISSION_PREFIX = "android.permission.health.WRITE_"

        private val RECORD_TYPE_TO_PERMISSION =
            mapOf<KClass<out Record>, String>(
                ActiveCaloriesBurnedRecord::class to
                    READ_ACTIVE_CALORIES_BURNED.substringAfter(READ_PERMISSION_PREFIX),
                BasalBodyTemperatureRecord::class to
                    READ_BASAL_BODY_TEMPERATURE.substringAfter(READ_PERMISSION_PREFIX),
                BasalMetabolicRateRecord::class to
                    READ_BASAL_METABOLIC_RATE.substringAfter(READ_PERMISSION_PREFIX),
                BloodGlucoseRecord::class to
                    READ_BLOOD_GLUCOSE.substringAfter(READ_PERMISSION_PREFIX),
                BloodPressureRecord::class to
                    READ_BLOOD_PRESSURE.substringAfter(READ_PERMISSION_PREFIX),
                BodyFatRecord::class to READ_BODY_FAT.substringAfter(READ_PERMISSION_PREFIX),
                BodyTemperatureRecord::class to
                    READ_BODY_TEMPERATURE.substringAfter(READ_PERMISSION_PREFIX),
                BodyWaterMassRecord::class to
                    READ_BODY_WATER_MASS.substringAfter(READ_PERMISSION_PREFIX),
                BoneMassRecord::class to READ_BONE_MASS.substringAfter(READ_PERMISSION_PREFIX),
                CervicalMucusRecord::class to
                    READ_CERVICAL_MUCUS.substringAfter(READ_PERMISSION_PREFIX),
                CyclingPedalingCadenceRecord::class to
                    READ_EXERCISE.substringAfter(READ_PERMISSION_PREFIX),
                DistanceRecord::class to READ_DISTANCE.substringAfter(READ_PERMISSION_PREFIX),
                ElevationGainedRecord::class to
                    READ_ELEVATION_GAINED.substringAfter(READ_PERMISSION_PREFIX),
                ExerciseEventRecord::class to READ_EXERCISE.substringAfter(READ_PERMISSION_PREFIX),
                ExerciseLapRecord::class to READ_EXERCISE.substringAfter(READ_PERMISSION_PREFIX),
                ExerciseRepetitionsRecord::class to
                    READ_EXERCISE.substringAfter(READ_PERMISSION_PREFIX),
                ExerciseSessionRecord::class to
                    READ_EXERCISE.substringAfter(READ_PERMISSION_PREFIX),
                FloorsClimbedRecord::class to
                    READ_FLOORS_CLIMBED.substringAfter(READ_PERMISSION_PREFIX),
                HeartRateRecord::class to READ_HEART_RATE.substringAfter(READ_PERMISSION_PREFIX),
                HeartRateVariabilityDifferentialIndexRecord::class to
                    READ_HEART_RATE_VARIABILITY.substringAfter(READ_PERMISSION_PREFIX),
                HeartRateVariabilityRmssdRecord::class to
                    READ_HEART_RATE_VARIABILITY.substringAfter(READ_PERMISSION_PREFIX),
                HeartRateVariabilitySd2Record::class to
                    READ_HEART_RATE_VARIABILITY.substringAfter(READ_PERMISSION_PREFIX),
                HeartRateVariabilitySdannRecord::class to
                    READ_HEART_RATE_VARIABILITY.substringAfter(READ_PERMISSION_PREFIX),
                HeartRateVariabilitySdnnIndexRecord::class to
                    READ_HEART_RATE_VARIABILITY.substringAfter(READ_PERMISSION_PREFIX),
                HeartRateVariabilitySdnnRecord::class to
                    READ_HEART_RATE_VARIABILITY.substringAfter(READ_PERMISSION_PREFIX),
                HeartRateVariabilitySdsdRecord::class to
                    READ_HEART_RATE_VARIABILITY.substringAfter(READ_PERMISSION_PREFIX),
                HeartRateVariabilitySRecord::class to
                    READ_HEART_RATE_VARIABILITY.substringAfter(READ_PERMISSION_PREFIX),
                HeartRateVariabilityTinnRecord::class to
                    READ_HEART_RATE_VARIABILITY.substringAfter(READ_PERMISSION_PREFIX),
                HeightRecord::class to READ_HEIGHT.substringAfter(READ_PERMISSION_PREFIX),
                HipCircumferenceRecord::class to
                    READ_HIP_CIRCUMFERENCE.substringAfter(READ_PERMISSION_PREFIX),
                HydrationRecord::class to READ_HYDRATION.substringAfter(READ_PERMISSION_PREFIX),
                LeanBodyMassRecord::class to
                    READ_LEAN_BODY_MASS.substringAfter(READ_PERMISSION_PREFIX),
                MenstruationFlowRecord::class to
                    READ_MENSTRUATION.substringAfter(READ_PERMISSION_PREFIX),
                NutritionRecord::class to READ_NUTRITION.substringAfter(READ_PERMISSION_PREFIX),
                OvulationTestRecord::class to
                    READ_OVULATION_TEST.substringAfter(READ_PERMISSION_PREFIX),
                OxygenSaturationRecord::class to
                    READ_OXYGEN_SATURATION.substringAfter(READ_PERMISSION_PREFIX),
                PowerRecord::class to READ_POWER.substringAfter(READ_PERMISSION_PREFIX),
                RespiratoryRateRecord::class to
                    READ_RESPIRATORY_RATE.substringAfter(READ_PERMISSION_PREFIX),
                RestingHeartRateRecord::class to
                    READ_RESTING_HEART_RATE.substringAfter(READ_PERMISSION_PREFIX),
                SexualActivityRecord::class to
                    READ_SEXUAL_ACTIVITY.substringAfter(READ_PERMISSION_PREFIX),
                SleepSessionRecord::class to READ_SLEEP.substringAfter(READ_PERMISSION_PREFIX),
                SleepStageRecord::class to READ_SLEEP.substringAfter(READ_PERMISSION_PREFIX),
                SpeedRecord::class to READ_SPEED.substringAfter(READ_PERMISSION_PREFIX),
                StepsCadenceRecord::class to READ_STEPS.substringAfter(READ_PERMISSION_PREFIX),
                StepsRecord::class to READ_STEPS.substringAfter(READ_PERMISSION_PREFIX),
                SwimmingStrokesRecord::class to
                    READ_EXERCISE.substringAfter(READ_PERMISSION_PREFIX),
                TotalCaloriesBurnedRecord::class to
                    READ_TOTAL_CALORIES_BURNED.substringAfter(READ_PERMISSION_PREFIX),
                Vo2MaxRecord::class to READ_VO2_MAX.substringAfter(READ_PERMISSION_PREFIX),
                WaistCircumferenceRecord::class to
                    READ_WAIST_CIRCUMFERENCE.substringAfter(READ_PERMISSION_PREFIX),
                WeightRecord::class to READ_WEIGHT.substringAfter(READ_PERMISSION_PREFIX),
                WheelchairPushesRecord::class to
                    READ_WHEELCHAIR_PUSHES.substringAfter(READ_PERMISSION_PREFIX),
            )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HealthPermission) return false

        if (recordType != other.recordType) return false
        if (accessType != other.accessType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = recordType.hashCode()
        result = 31 * result + accessType
        return result
    }
}
