package com.project.expressfood

import android.app.Application
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Constraints
import androidx.work.WorkManager
import com.project.expressfood.data.work.SyncOrdersWorker
import com.project.expressfood.data.work.SyncOrdersWorkerFactory
import com.project.expressfood.di.AppContainer
import java.util.concurrent.TimeUnit

class ExpressFoodApp : Application(), Configuration.Provider {

    /** Contenedor de dependencias (DI manual). Accesible desde cualquier Activity/Fragment. */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        schedulePeriodicSync()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(SyncOrdersWorkerFactory(container.orderRepository))
            .build()

    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<SyncOrdersWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sync_pending_orders",
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}