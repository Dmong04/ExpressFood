package com.project.expressfood.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.project.expressfood.data.repository.OrderRepository

class SyncOrdersWorker(
    context: Context,
    params: WorkerParameters,
    private val orderRepository: OrderRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            orderRepository.syncPendingOrders()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}