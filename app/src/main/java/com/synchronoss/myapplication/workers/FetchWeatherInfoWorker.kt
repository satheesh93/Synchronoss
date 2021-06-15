package com.synchronoss.myapplication.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters


class FetchWeatherInfoWorker(val context: Context, params: WorkerParameters) :
    Worker(context, params) {

    override fun doWork(): Result {
        return Result.failure()
    }

}