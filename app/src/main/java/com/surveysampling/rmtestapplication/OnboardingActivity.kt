package com.surveysampling.rmtestapplication

import android.support.v7.app.AppCompatActivity
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import com.realitymine.usagemonitor.android.UMSDK
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_onboarding.*

class OnboardingActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        // Make sure that the permission statuses are initialised. This can only be done once the
        // registration sequence is complete, as the states depend on project settings that are
        // received during registration.
        UMSDK.prepareForOnboarding()
    }

    override fun onResume() {
        super.onResume()
        // Hide or disable the permission buttons depending on the state of the permissions
        setButtonState(androidPermissionsButton, getAndroidPermissionStatus())
        setButtonState(vpnPermissionButton, UMSDK.getVPNPermissionStatus())
        setButtonState(installCertificateButton, UMSDK.getCertificatePermissionStatus())
        setButtonState(accessibilityPermissionButton, UMSDK.getAccessibilityPermissionStatus())
        setButtonState(usageStatsPermissionButton, UMSDK.getUsageStatsPermissionStatus())
        // The last button is used to tell the SDK to start monitoring.
        // You may want to disable this until all permissions have been accepted.
        startMonitoringButton.setEnabled(!UMSDK.areTermsAgreed())
    }

    /**
     * Button handler to accept the Android runtime permissions
     */
    fun onAndroidPermissionsButton(v: View) {
        promptForAndroidPermissions(this@OnboardingActivity)
    }

    /**
     * Button handler to accept the VPN permission
     */
    fun onVpnPermissionButton(v: View) {
        UMSDK.showVPNPermissionActivity(this@OnboardingActivity)
    }

    /**
     * Button handler to install the VPN certificate
     */
    fun onInstallCertificateButton(v: View) {
        UMSDK.showCertificatePermissionActivity(this@OnboardingActivity)
    }

    /**
     * Button handler to accept the accessibility permission
     */
    fun onAccessibilityPermissionButton(v: View) {
        UMSDK.showAccessibilityPermissionActivity(this@OnboardingActivity)
    }

    /**
     * Button handler to accept the usage stats permission
     */
    fun onUsageStatsPermissionButton(v: View) {
        UMSDK.showUsageStatsPermissionActivity(this@OnboardingActivity)
    }

    /**
     * Button handler to start monitoring
     */
    fun onStartMonitoringButton(v: View) {
        // This method starts the monitoring process. The user should have and accepted all terms
        // and conditions before this is called.
        UMSDK.acceptedTerms()
        finish()
    }

    /**
     * Set the state of a button based on the supplied permission status.
     */
    private fun setButtonState(button: Button, status: UMSDK.PermissionStatus) {
        if (status == UMSDK.PermissionStatus.FEATURE_NOT_ENABLED) {
            // The project settings mean that this permission is not required
            button.setVisibility(View.GONE)
        } else {
            // This permission is required; enable the button if we do not yet have it
            button.setEnabled(status == UMSDK.PermissionStatus.PERMISSION_REQUIRED)
        }
    }

    /**
     * Determine the state of the Android runtime permissions.
     */
    private fun getAndroidPermissionStatus(): UMSDK.PermissionStatus {
        // Runtime permissions were added at API 23; and so are not required before this
        if (android.os.Build.VERSION.SDK_INT < 23) {
            return UMSDK.PermissionStatus.FEATURE_NOT_ENABLED
        }
        // You may want to add your own permissions to this list
        val permissions = UMSDK.getAndroidPermissionsForMonitoring()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return UMSDK.PermissionStatus.PERMISSION_REQUIRED
            }
        }
        return UMSDK.PermissionStatus.PERMISSION_GRANTED
    }

    companion object {
        /**
         * Prompt for all missing Android runtime permissions.
         * Note: this method is also called from the MainActivity to handle the permission notification
         */
        fun promptForAndroidPermissions(activity: Activity) {
            // You may want to add your own permissions to this list
            val requiredPermissions = UMSDK.getAndroidPermissionsForMonitoring()
            val needPromptPermissions = ArrayList<String>()
            for (permission in requiredPermissions) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    needPromptPermissions.add(permission)
                }
            }
            if (needPromptPermissions.size > 0) {
                val permissionsArray = needPromptPermissions.toArray(arrayOfNulls<String>(0))
                ActivityCompat.requestPermissions(activity, permissionsArray, 0)
            }
        }
    }
}