package com.example.healthconnectsample.data

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.aggregate.AggregationResultGroupedByPeriod
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.request.AggregateGroupByDurationRequest
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ChangesResponse
import androidx.health.connect.client.response.InsertRecordsResponse
import androidx.health.connect.client.response.ReadRecordResponse
import androidx.health.connect.client.response.ReadRecordsResponse
import androidx.health.connect.client.time.TimeRangeFilter
import kotlin.reflect.KClass

class FakeHealthConnectManager : HealthConnectClient {

    override lateinit var permissionController: PermissionController

    override suspend fun aggregate(request: AggregateRequest): AggregationResult {
        TODO("not implemented")
    }

    override suspend fun aggregateGroupByDuration(request: AggregateGroupByDurationRequest): List<AggregationResultGroupedByDuration> {
        TODO("not implemented")
    }

    override suspend fun aggregateGroupByPeriod(request: AggregateGroupByPeriodRequest): List<AggregationResultGroupedByPeriod> {
        TODO("not implemented")
    }

    override suspend fun deleteRecords(
        recordType: KClass<out Record>,
        timeRangeFilter: TimeRangeFilter
    ) {
        TODO("not implemented")
    }

    override suspend fun deleteRecords(
        recordType: KClass<out Record>,
        recordIdsList: List<String>,
        clientRecordIdsList: List<String>
    ) {
        TODO("not implemented")
    }

    override suspend fun getChanges(changesToken: String): ChangesResponse {
        TODO("not implemented")
    }

    override suspend fun getChangesToken(request: ChangesTokenRequest): String {
        TODO("not implemented")
    }

    override suspend fun insertRecords(records: List<Record>): InsertRecordsResponse {
        TODO("not implemented")
    }

    override suspend fun <T : Record> readRecord(
        recordType: KClass<T>,
        recordId: String
    ): ReadRecordResponse<T> {
        TODO("not implemented")
    }

    override suspend fun <T : Record> readRecords(request: ReadRecordsRequest<T>): ReadRecordsResponse<T> {
        TODO("not implemented")
    }

    override suspend fun updateRecords(records: List<Record>) {
        TODO("not implemented")
    }
}
