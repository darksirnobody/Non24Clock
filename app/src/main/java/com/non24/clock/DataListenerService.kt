package com.non24.clock

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class DataListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "DataListenerService"
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "onDataChanged called with ${dataEvents.count} events")

        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val uri = event.dataItem.uri
                Log.d(TAG, "Data changed at path: ${uri.path}")

                if (uri.path == "/non24_config") {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

                    val anchorTime = dataMap.getLong("anchor_time")
                    val cycleLengthMs = dataMap.getLong("cycle_length_ms")
                    val swapClocks = dataMap.getBoolean("swap_clocks")

                    Log.d(TAG, "Received: anchor=$anchorTime, cycle=$cycleLengthMs, swap=$swapClocks")

                    val prefs = getSharedPreferences("non24_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putLong("anchor_time", anchorTime)
                        .putLong("cycle_length_ms", cycleLengthMs)
                        .putBoolean("swap_clocks", swapClocks)
                        .apply()

                    Log.d(TAG, "Saved to SharedPreferences")
                }
            }
        }
    }
}