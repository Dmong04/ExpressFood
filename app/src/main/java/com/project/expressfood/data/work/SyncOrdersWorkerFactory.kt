package com.project.expressfood.data.work

import android.content.Context
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import com.project.expressfood.data.repository.OrderRepository

class SyncOrdersWorkerFactory(
    private val orderRepository: OrderRepository,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        return when (workerClassName) {
            SyncOrdersWorker::class.java.name ->
                SyncOrdersWorker(appContext, workerParameters, orderRepository)
            else -> null
        }
    }
}