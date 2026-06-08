package com.project.expressfood

import android.app.Application
import com.project.expressfood.di.AppContainer

class ExpressFoodApp : Application() {

    /** Contenedor de dependencias (DI manual). Accesible desde cualquier Activity/Fragment. */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
