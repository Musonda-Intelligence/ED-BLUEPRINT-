package com.edblueprint.app

import android.app.Application

class EdBlueprintApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
