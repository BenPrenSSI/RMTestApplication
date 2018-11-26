package com.surveysampling.rmtestapplication

import android.app.Application
import com.realitymine.usagemonitor.android.UMSDK



class TestApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        UMSDK.activateSDK(this, "SDK Sample App", SampleNotificationProvider())

    }

}