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

@file:RequiresApi(api = 34)

package androidx.health.connect.client.impl.platform.aggregate

import androidx.annotation.RequiresApi
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Pressure
import kotlin.math.max
import kotlin.math.min

private val BLOOD_PRESSURE_METRICS = setOf(
    BloodPressureRecord.DIASTOLIC_AVG,
    BloodPressureRecord.DIASTOLIC_MAX,
    BloodPressureRecord.DIASTOLIC_MIN,
    BloodPressureRecord.SYSTOLIC_AVG,
    BloodPressureRecord.SYSTOLIC_MAX,
    BloodPressureRecord.SYSTOLIC_MIN,
)

internal suspend fun HealthConnectClient.aggregateBloodPressure(
    bloodPressureMetrics: Set<AggregateMetric<*>>,
    timeRangeFilter: TimeRangeFilter,
    dataOriginFilter: Set<DataOrigin>
): AggregationResult {
    check(BLOOD_PRESSURE_METRICS.containsAll(bloodPressureMetrics)) {
        "Invalid set of blood pressure metrics $bloodPressureMetrics"
    }

    if (bloodPressureMetrics.isEmpty()) {
        return emptyAggregationResult()
    }

    val readRecordsFlow = readRecordsFlow(
        BloodPressureRecord::class,
        timeRangeFilter,
        dataOriginFilter
    )

    val avgDataMap = mutableMapOf<AggregateMetric<Pressure>, AvgData>()
    val minMaxMap = mutableMapOf<AggregateMetric<Pressure>, Double?>()

    for (metric in bloodPressureMetrics) {
        when (metric) {
            BloodPressureRecord.DIASTOLIC_AVG, BloodPressureRecord.SYSTOLIC_AVG ->
                avgDataMap[metric] = AvgData()

            BloodPressureRecord.DIASTOLIC_MAX, BloodPressureRecord.DIASTOLIC_MIN,
            BloodPressureRecord.SYSTOLIC_MAX, BloodPressureRecord.SYSTOLIC_MIN ->
                minMaxMap[metric] = null

            else -> error("Invalid blood pressure fallback aggregation type ${metric.metricKey}")
        }
    }

    val dataOrigins = mutableSetOf<DataOrigin>()

    readRecordsFlow.collect { records ->
        records.forEach {
            val diastolic = it.diastolic.inMillimetersOfMercury
            val systolic = it.systolic.inMillimetersOfMercury

            for (metric in bloodPressureMetrics) {
                when (metric) {
                    BloodPressureRecord.DIASTOLIC_AVG -> avgDataMap[metric]!! += diastolic
                    BloodPressureRecord.DIASTOLIC_MAX -> minMaxMap[metric] =
                        max(minMaxMap[metric] ?: diastolic, diastolic)

                    BloodPressureRecord.DIASTOLIC_MIN -> minMaxMap[metric] =
                        min(minMaxMap[metric] ?: diastolic, diastolic)

                    BloodPressureRecord.SYSTOLIC_AVG -> avgDataMap[metric]!! += systolic
                    BloodPressureRecord.SYSTOLIC_MAX -> minMaxMap[metric] =
                        max(minMaxMap[metric] ?: systolic, systolic)

                    BloodPressureRecord.SYSTOLIC_MIN -> minMaxMap[metric] =
                        min(minMaxMap[metric] ?: systolic, systolic)
                }
            }

            dataOrigins += it.metadata.dataOrigin
        }
    }

    if (dataOrigins.isEmpty()) {
        return emptyAggregationResult()
    }

    val doubleValues = buildMap {
        for (metric in bloodPressureMetrics) {
            val aggregatedValue = when (metric) {
                BloodPressureRecord.DIASTOLIC_AVG, BloodPressureRecord.SYSTOLIC_AVG ->
                    avgDataMap[metric]!!.average()

                BloodPressureRecord.DIASTOLIC_MAX, BloodPressureRecord.DIASTOLIC_MIN,
                BloodPressureRecord.SYSTOLIC_MAX, BloodPressureRecord.SYSTOLIC_MIN ->
                    minMaxMap[metric]!!

                else ->
                    error("Invalid blood pressure fallback aggregation type ${metric.metricKey}")
            }

            put(metric.metricKey, aggregatedValue)
        }
    }

    return AggregationResult(
        longValues = mapOf(),
        doubleValues = doubleValues,
        dataOrigins = dataOrigins
    )
}

private data class AvgData(var count: Int = 0, var total: Double = 0.0) {
    operator fun plusAssign(value: Double) {
        count++
        total += value
    }

    fun average() = total / count
}
