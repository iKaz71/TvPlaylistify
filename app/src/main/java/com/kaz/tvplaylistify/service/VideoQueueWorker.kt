package com.kaz.tvplaylistify.service

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kaz.tvplaylistify.util.VideoSender

class VideoQueueWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.d("VideoQueueWorker", "Iniciando reproducción desde Worker")
        VideoSender.reproducirSiguiente(applicationContext)
        return Result.success()
    }
}
