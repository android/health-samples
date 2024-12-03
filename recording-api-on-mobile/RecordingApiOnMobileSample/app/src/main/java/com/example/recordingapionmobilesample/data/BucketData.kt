package com.example.recordingapionmobilesample.data

data class BucketData(
    val index: Int,
    val startTime: Long,
    val endTime: Long,
    val dataSetDataList: List<DataSetData>
)