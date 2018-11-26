package com.surveysampling.rmtestapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.realitymine.usagemonitor.android.UMSDK
import com.realitymine.usagemonitor.android.UMBroadcasts
import android.content.BroadcastReceiver
import android.content.Context
import android.support.v4.content.LocalBroadcastManager
import android.content.IntentFilter
import android.view.View
import java.util.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Additional processing if the activity was launched from a notification
        handleLaunchFromNotification(intent)
    }

    override fun onPause() {
        super.onPause()
        // Stop broadcasts from the SDK whilst the application is paused
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
    }

    override fun onResume() {
        super.onResume()

        // Register for broadcasts from the SDK. For this activity we only want to know when the
        // SDK state changes
        val filter = IntentFilter(UMBroadcasts.ACTION_UPDATE_STATE)
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter)

        // Request a resend of any broadcasts which may have been missed whilst the application was
        // paused
        UMSDK.resendUnacknowledgedBroadcasts()
        updateUI()
    }

    /**
     * Receiver for local broadcasts from the SDK.
     */
    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null) {
                var handledBroadcast = true
                when (action) {
                    UMBroadcasts.ACTION_UPDATE_STATE -> updateUI()
                    else -> handledBroadcast = false
                }
                if (handledBroadcast) {
                    // Note: if you have multiple activities handling broadcasts then each activity
                    // should only acknowledge the broadcasts it is handled.
                    UMSDK.acknowledgeBroadcast(intent)
                }
            }
        }
    }

    /**
     * Additional processing if the activity was launched from one of the notifications created by
     * the SampleNotificationProvider class
     */
    private fun handleLaunchFromNotification(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (action != null) {
                when (action) {
                    SampleNotificationProvider.ENABLE_ACCESSIBILITY -> UMSDK.showAccessibilityPermissionActivity(this)
                    SampleNotificationProvider.ENABLE_USAGE_STATS -> UMSDK.showUsageStatsPermissionActivity(this)
                    SampleNotificationProvider.GRANT_PERMISSIONS -> OnboardingActivity.promptForAndroidPermissions(this)
                }
            }
        }
    }

    /**
     * This method shows how you might update your UI in response to SDK status changes
     */
    private fun updateUI() {
        var statusText = ""
        if (UMSDK.isUserRegistered()) {
            statusText += "Registered to: " + UMSDK.getDisplayName()
        } else {
            statusText += "Not registered"
        }
        statusText += "\n"
        if (UMSDK.areTermsAgreed()) {
            statusText += "Last upload: " + Date(UMSDK.getLastUploadTime())
        } else {
            statusText += "Not monitoring"
        }
        statusTextView.text = statusText
    }

    /**
     * Event handler to open the registration activity
     */
    fun onRegisterButton(v: View) {
        startActivity(Intent(this, RegistrationActivity::class.java))
    }
}
