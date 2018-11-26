package com.surveysampling.rmtestapplication

import android.support.v7.app.AppCompatActivity
import com.realitymine.usagemonitor.android.UMSDK
import android.widget.Toast
import com.realitymine.usagemonitor.android.UMNetworkError
import com.realitymine.usagemonitor.android.UMBroadcasts
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.support.v4.content.LocalBroadcastManager
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_registration.*

class RegistrationActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
    }

    override fun onPause() {
        super.onPause()
        // Stop broadcasts from the SDK whilst the application is paused
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
    }

    override fun onResume() {
        super.onResume()

        // Register for broadcasts from the SDK. For this activity we only want to know about
        // the registration result
        val filter = IntentFilter()
        filter.addAction(UMBroadcasts.ACTION_REGISTRATION_SUCCEEDED)
        filter.addAction(UMBroadcasts.ACTION_REGISTRATION_FAILED)
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter)

        // Request a resend of any broadcasts which may have been missed whilst the application was
        // paused
        UMSDK.resendUnacknowledgedBroadcasts()
    }

    /**
     * Event handler to register the user
     */
    fun onRegisterButton(v: View) {
        // Obtain user name and (if needed by your project setup) password
        val username = usernameEditText.text.toString()
//        val password: String? = null
        // Disable register button and show progress indicator while registration completes
        register_button.isEnabled = false
        progressBar.visibility = View.VISIBLE
        // Start the registration process. This starts an asynchronous process that will take a few
        // seconds. You will receive a local broadcast when the process completes.
        UMSDK.registerWithProvId(username)
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
                    UMBroadcasts.ACTION_REGISTRATION_SUCCEEDED -> {
                        // The registration sequence is complete. We can now start the onboarding
                        // process and close this activity
                        startActivity(Intent(context, OnboardingActivity::class.java))
                        finish()
                    }
                    UMBroadcasts.ACTION_REGISTRATION_FAILED -> {
                        // Fetch the error code from the intent. For a list of error codes see
                        // the UMNetworkError class.
                        val errorCode = intent.getIntExtra(
                            UMBroadcasts.EXTRA_REGISTRATION_FAILURE_REASON,
                            UMNetworkError.UNKNOWN
                        )
                        Toast.makeText(
                            this@RegistrationActivity,
                            "Registration failed with code $errorCode",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Re-enable register button and hide progress indicator
                        register_button.isEnabled = true
                        progressBar.visibility = View.GONE
                    }
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
}