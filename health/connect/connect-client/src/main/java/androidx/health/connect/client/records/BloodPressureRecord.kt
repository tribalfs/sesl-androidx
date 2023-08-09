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
package androidx.health.connect.client.records

import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.records.BloodPressureRecord.MeasurementLocation
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Pressure
import androidx.health.connect.client.units.millimetersOfMercury
import java.time.Instant
import java.time.ZoneOffset

/**
 * Captures the blood pressure of a user. Each record represents a single instantaneous blood
 * pressure reading.
 */
public class BloodPressureRecord(
    override val time: Instant,
    override val zoneOffset: ZoneOffset?,
    /**
     * Systolic blood pressure measurement, in [Pressure] unit. Required field. Valid range: 20-200
     * mmHg.
     */
    public val systolic: Pressure,
    /**
     * Diastolic blood pressure measurement, in [Pressure] unit. Required field. Valid range: 10-180
     * mmHg.
     */
    public val diastolic: Pressure,
    /**
     * The user's body position when the measurement was taken. Optional field. Allowed values:
     * [BodyPosition].
     *
     * @see BodyPosition
     */
    @property:BodyPositions public val bodyPosition: Int = BODY_POSITION_UNKNOWN,
    /**
     * The arm and part of the arm where the measurement was taken. Optional field. Allowed values:
     * [MeasurementLocation].
     *
     * @see MeasurementLocation
     */
    @property:MeasurementLocations
    public val measurementLocation: Int = MEASUREMENT_LOCATION_UNKNOWN,
    override val metadata: Metadata = Metadata.EMPTY,
) : InstantaneousRecord {

    init {
        systolic.requireNotLess(other = MIN_SYSTOLIC, name = "systolic")
        systolic.requireNotMore(other = MAX_SYSTOLIC, name = "systolic")
        diastolic.requireNotLess(other = MIN_DIASTOLIC, name = "diastolic")
        diastolic.requireNotMore(other = MAX_DIASTOLIC, name = "diastolic")
    }

    /*
     * Generated by the IDE: Code -> Generate -> "equals() and hashCode()".
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BloodPressureRecord) return false

        if (systolic != other.systolic) return false
        if (diastolic != other.diastolic) return false
        if (bodyPosition != other.bodyPosition) return false
        if (measurementLocation != other.measurementLocation) return false
        if (time != other.time) return false
        if (zoneOffset != other.zoneOffset) return false
        if (metadata != other.metadata) return false

        return true
    }

    /*
     * Generated by the IDE: Code -> Generate -> "equals() and hashCode()".
     */
    override fun hashCode(): Int {
        var result = systolic.hashCode()
        result = 31 * result + diastolic.hashCode()
        result = 31 * result + bodyPosition
        result = 31 * result + measurementLocation
        result = 31 * result + time.hashCode()
        result = 31 * result + (zoneOffset?.hashCode() ?: 0)
        result = 31 * result + metadata.hashCode()
        return result
    }

    /** The arm and part of the arm where a blood pressure measurement was taken. */
    internal object MeasurementLocation {
        const val LEFT_WRIST = "left_wrist"
        const val RIGHT_WRIST = "right_wrist"
        const val LEFT_UPPER_ARM = "left_upper_arm"
        const val RIGHT_UPPER_ARM = "right_upper_arm"
    }

    /**
     * The user's body position when a health measurement is taken.
     */
    internal object BodyPosition {
        const val STANDING_UP = "standing_up"
        const val SITTING_DOWN = "sitting_down"
        const val LYING_DOWN = "lying_down"
        const val RECLINING = "reclining"
    }

    /**
     * The arm and part of the arm where a blood pressure measurement was taken.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(
        value =
            [
                MEASUREMENT_LOCATION_UNKNOWN,
                MEASUREMENT_LOCATION_LEFT_WRIST,
                MEASUREMENT_LOCATION_RIGHT_WRIST,
                MEASUREMENT_LOCATION_LEFT_UPPER_ARM,
                MEASUREMENT_LOCATION_RIGHT_UPPER_ARM
            ]
    )
    annotation class MeasurementLocations

    /**
     * The user's body position when a health measurement is taken.
     */
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(
        value =
            [
                BODY_POSITION_UNKNOWN,
                BODY_POSITION_STANDING_UP,
                BODY_POSITION_SITTING_DOWN,
                BODY_POSITION_LYING_DOWN,
                BODY_POSITION_RECLINING
            ]
    )
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    annotation class BodyPositions

    companion object {

        const val MEASUREMENT_LOCATION_UNKNOWN = 0
        const val MEASUREMENT_LOCATION_LEFT_WRIST = 1
        const val MEASUREMENT_LOCATION_RIGHT_WRIST = 2
        const val MEASUREMENT_LOCATION_LEFT_UPPER_ARM = 3
        const val MEASUREMENT_LOCATION_RIGHT_UPPER_ARM = 4

        const val BODY_POSITION_UNKNOWN = 0
        const val BODY_POSITION_STANDING_UP = 1
        const val BODY_POSITION_SITTING_DOWN = 2
        const val BODY_POSITION_LYING_DOWN = 3
        const val BODY_POSITION_RECLINING = 4

        @RestrictTo(RestrictTo.Scope.LIBRARY)
        @JvmField
        val MEASUREMENT_LOCATION_STRING_TO_INT_MAP: Map<String, Int> =
            mapOf(
                MeasurementLocation.LEFT_UPPER_ARM to MEASUREMENT_LOCATION_LEFT_UPPER_ARM,
                MeasurementLocation.LEFT_WRIST to MEASUREMENT_LOCATION_LEFT_WRIST,
                MeasurementLocation.RIGHT_UPPER_ARM to MEASUREMENT_LOCATION_RIGHT_UPPER_ARM,
                MeasurementLocation.RIGHT_WRIST to MEASUREMENT_LOCATION_RIGHT_WRIST
            )

        @RestrictTo(RestrictTo.Scope.LIBRARY)
        @JvmField
        val MEASUREMENT_LOCATION_INT_TO_STRING_MAP =
            MEASUREMENT_LOCATION_STRING_TO_INT_MAP.reverse()

        @RestrictTo(RestrictTo.Scope.LIBRARY)
        @JvmField
        val BODY_POSITION_STRING_TO_INT_MAP: Map<String, Int> =
            mapOf(
                BodyPosition.LYING_DOWN to BODY_POSITION_LYING_DOWN,
                BodyPosition.RECLINING to BODY_POSITION_RECLINING,
                BodyPosition.SITTING_DOWN to BODY_POSITION_SITTING_DOWN,
                BodyPosition.STANDING_UP to BODY_POSITION_STANDING_UP
            )

        @RestrictTo(RestrictTo.Scope.LIBRARY)
        @JvmField
        val BODY_POSITION_INT_TO_STRING_MAP = BODY_POSITION_STRING_TO_INT_MAP.reverse()

        private const val BLOOD_PRESSURE_NAME = "BloodPressure"
        private const val SYSTOLIC_FIELD_NAME = "systolic"
        private const val DIASTOLIC_FIELD_NAME = "diastolic"
        private val MIN_SYSTOLIC = 20.millimetersOfMercury
        private val MAX_SYSTOLIC = 200.millimetersOfMercury
        private val MIN_DIASTOLIC = 10.millimetersOfMercury
        private val MAX_DIASTOLIC = 180.millimetersOfMercury

        /**
         * Metric identifier to retrieve average systolic from
         * [androidx.health.connect.client.aggregate.AggregationResult].
         */
        @JvmField
        val SYSTOLIC_AVG: AggregateMetric<Pressure> =
            AggregateMetric.doubleMetric(
                dataTypeName = BLOOD_PRESSURE_NAME,
                aggregationType = AggregateMetric.AggregationType.AVERAGE,
                fieldName = SYSTOLIC_FIELD_NAME,
                mapper = Pressure::millimetersOfMercury,
            )

        /**
         * Metric identifier to retrieve minimum systolic from
         * [androidx.health.connect.client.aggregate.AggregationResult].
         */
        @JvmField
        val SYSTOLIC_MIN: AggregateMetric<Pressure> =
            AggregateMetric.doubleMetric(
                dataTypeName = BLOOD_PRESSURE_NAME,
                aggregationType = AggregateMetric.AggregationType.MINIMUM,
                fieldName = SYSTOLIC_FIELD_NAME,
                mapper = Pressure::millimetersOfMercury,
            )

        /**
         * Metric identifier to retrieve maximum systolic from
         * [androidx.health.connect.client.aggregate.AggregationResult].
         */
        @JvmField
        val SYSTOLIC_MAX: AggregateMetric<Pressure> =
            AggregateMetric.doubleMetric(
                dataTypeName = BLOOD_PRESSURE_NAME,
                aggregationType = AggregateMetric.AggregationType.MAXIMUM,
                fieldName = SYSTOLIC_FIELD_NAME,
                mapper = Pressure::millimetersOfMercury,
            )

        /**
         * Metric identifier to retrieve average diastolic from
         * [androidx.health.connect.client.aggregate.AggregationResult].
         */
        @JvmField
        val DIASTOLIC_AVG: AggregateMetric<Pressure> =
            AggregateMetric.doubleMetric(
                dataTypeName = BLOOD_PRESSURE_NAME,
                aggregationType = AggregateMetric.AggregationType.AVERAGE,
                fieldName = DIASTOLIC_FIELD_NAME,
                mapper = Pressure::millimetersOfMercury,
            )

        /**
         * Metric identifier to retrieve minimum diastolic from
         * [androidx.health.connect.client.aggregate.AggregationResult].
         */
        @JvmField
        val DIASTOLIC_MIN: AggregateMetric<Pressure> =
            AggregateMetric.doubleMetric(
                dataTypeName = BLOOD_PRESSURE_NAME,
                aggregationType = AggregateMetric.AggregationType.MINIMUM,
                fieldName = DIASTOLIC_FIELD_NAME,
                mapper = Pressure::millimetersOfMercury,
            )

        /**
         * Metric identifier to retrieve maximum diastolic from
         * [androidx.health.connect.client.aggregate.AggregationResult].
         */
        @JvmField
        val DIASTOLIC_MAX: AggregateMetric<Pressure> =
            AggregateMetric.doubleMetric(
                dataTypeName = BLOOD_PRESSURE_NAME,
                aggregationType = AggregateMetric.AggregationType.MAXIMUM,
                fieldName = DIASTOLIC_FIELD_NAME,
                mapper = Pressure::millimetersOfMercury,
            )
    }
}
