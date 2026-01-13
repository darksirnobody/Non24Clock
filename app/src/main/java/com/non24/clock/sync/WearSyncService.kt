package com.non24.clock.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

object WearSyncService {

    private const val TAG = "WearSyncService"
    private const val PATH_CONFIG = "/non24_config"

    fun syncToWatch(context: Context, anchorTime: Long, cycleLengthMs: Long, swapClocks: Boolean = false) {
        Log.d(TAG, "syncToWatch called: anchor=$anchorTime, cycle=$cycleLengthMs")

        val dataClient = Wearable.getDataClient(context)

        val putDataMapRequest = PutDataMapRequest.create(PATH_CONFIG).apply {
            dataMap.putLong("anchor_time", anchorTime)
            dataMap.putLong("cycle_length_ms", cycleLengthMs)
            dataMap.putBoolean("swap_clocks", swapClocks)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }

        val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()

        dataClient.putDataItem(putDataRequest)
            .addOnSuccessListener {
                Log.d(TAG, "Data sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to send data", e)
            }
    }
}