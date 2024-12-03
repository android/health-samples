package com.example.recordingapionmobilesample.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.recordingapionmobilesample.data.DataPointData
import com.example.recordingapionmobilesample.data.DataSetData
import java.time.LocalDateTime
import java.time.ZoneOffset

@Composable
fun DataSetRow(
    dataSetData: DataSetData
){
    Row (
        modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "DataSet #${dataSetData.index}")
            dataSetData.dataPointDataList.forEach { dataPointData ->
                DataPointRow(
                    dataPointData
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DataSetRowPreview(){
    DataSetRow(
        dataSetData = DataSetData(
            index = 0,
            dataPointDataList = listOf(
                DataPointData(
                    index = 0,
                    startTime = LocalDateTime
                        .now()
                        .minusDays(1)
                        .toEpochSecond(ZoneOffset.UTC),
                    endTime = LocalDateTime
                        .now()
                        .toEpochSecond(ZoneOffset.UTC),
                    fieldName = "Steps",
                    fieldValue = 23
                )
            )
        )
    )
}