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

import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Length
import androidx.health.connect.client.units.meters
import java.time.Instant
import java.time.ZoneOffset

/** Captures the user's height. */
public class HeightRecord(
    override val time: Instant,
    override val zoneOffset: ZoneOffset?,
    /** Height in [Length] unit. Required field. Valid range: 0-3 meters. */
    public val height: Length,
    override val metadata: Metadata = Metadata.EMPTY,
) : InstantaneousRecord {

    init {
        height.requireNotLess(other = height.zero(), name = "height")
        height.requireNotMore(other = MAX_HEIGHT, name = "height")
    }

    /*
     * Generated by the IDE: Code -> Generate -> "equals() and hashCode()".
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HeightRecord) return false

        if (height != other.height) return false
        if (time != other.time) return false
        if (zoneOffset != other.zoneOffset) return false
        if (metadata != other.metadata) return false

        return true
    }

    /*
     * Generated by the IDE: Code -> Generate -> "equals() and hashCode()".
     */
    override fun hashCode(): Int {
        var result = height.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + (zoneOffset?.hashCode() ?: 0)
        result = 31 * result + metadata.hashCode()
        return result
    }

    override fun toString(): String {
        return "HeightRecord(time=$time, zoneOffset=$zoneOffset, height=$height, metadata=$metadata)"
    }

    companion object {
        private const val HEIGHT_NAME = "Height"
        private const val HEIGHT_FIELD_NAME = "height"
        private val MAX_HEIGHT = 3.meters

        /**
         * Metric identifier to retrieve the average height from
         * [androidx.health.connect.client.aggregate.AggregationResult].
         */
        @JvmField
        val HEIGHT_AVG: AggregateMetric<Length> =
            AggregateMetric.doubleMetric(
                dataTypeName = HEIGHT_NAME,
                aggregationType = AggregateMetric.AggregationType.AVERAGE,
                fieldName = HEIGHT_FIELD_NAME,
                mapper = Length::meters,
            )

        /**
         * Metric identifier to retrieve minimum height from
         * [androidx.health.connect.client.aggregate.AggregationResult].
         */
        @JvmField
        val HEIGHT_MIN: AggregateMetric<Length> =
            AggregateMetric.doubleMetric(
                dataTypeName = HEIGHT_NAME,
                aggregationType = AggregateMetric.AggregationType.MINIMUM,
                fieldName = HEIGHT_FIELD_NAME,
                mapper = Length::meters,
            )

        /**
         * Metric identifier to retrieve the maximum height from
         * [androidx.health.connect.client.aggregate.AggregationResult].
         */
        @JvmField
        val HEIGHT_MAX: AggregateMetric<Length> =
            AggregateMetric.doubleMetric(
                dataTypeName = HEIGHT_NAME,
                aggregationType = AggregateMetric.AggregationType.MAXIMUM,
                fieldName = HEIGHT_FIELD_NAME,
                mapper = Length::meters,
            )
    }
}
