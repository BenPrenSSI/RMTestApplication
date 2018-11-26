package com.surveysampling.rmtestapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import com.realitymine.usagemonitor.android.UMNotificationProvider

class SampleNotificationProvider: UMNotificationProvider {

    companion object {
        // Application unique notification ids
        private val APP_RUNNING_NOTIFICATION_ID = 1
        private val CERTIFICATE_INSTALLATION_NOTIFICATION_ID = 2
        private val VPN_PERMISSION_NOTIFICATION_ID = 3
        private val VPN_FATAL_ERROR_NOTIFICATION_ID = 4
        private val USAGE_STATS_NOTIFICATION_ID = 5
        private val ACCESSIBILITY_NOTIFICATION_ID = 6
        private val GRANT_PERMISSIONS_NOTIFICATION_ID = 7
        // Actions to be processed in the MainActivity
        val ENABLE_ACCESSIBILITY = "ENABLE_ACCESSIBILITY"
        val ENABLE_USAGE_STATS = "ENABLE_USAGE_STATS"
        val GRANT_PERMISSIONS = "GRANT_PERMISSIONS"
        // Notification channels
        private val MAIN_CHANNEL_ID = "com.realitymine.sdksample.main_channel"
        private var mMainChannel: NotificationChannel? = null
    }

    override fun getServiceRunningNotification(context: Context): Notification {
        // Create an ongoing notification that will start the main activity when tapped
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        return buildNotification(context, "Sample SDK", "Service Running", pendingIntent, true)
    }

    override fun getServiceRunningNotificationId(): Int {
        // Return a unique id that is greater than zero
        return APP_RUNNING_NOTIFICATION_ID
    }

    override fun showVPNPermissionNotification(context: Context, permissionIntent: PendingIntent) {
        // Create notification that will launch the provided intent when tapped
        val notification = buildNotification(
            context, "VPN not connected",
            "Tap to reconnect", permissionIntent, false
        )
        showNotification(context, VPN_PERMISSION_NOTIFICATION_ID, notification)
    }

    override fun cancelVPNPermissionNotification(context: Context) {
        cancelNotification(context, VPN_PERMISSION_NOTIFICATION_ID)
    }

    override fun showVpnFatalErrorNotification(context: Context) {
        // Create notification that does nothing when tapped
        val notification = buildNotification(
            context, "VPN failure",
            "Please restart your device", null, false
        )
        showNotification(context, VPN_FATAL_ERROR_NOTIFICATION_ID, notification)
    }

    override fun cancelVpnFatalErrorNotification(context: Context) {
        cancelNotification(context, VPN_FATAL_ERROR_NOTIFICATION_ID)
    }

    override fun showInstallCertificateNotification(context: Context, installationIntent: PendingIntent) {
        // Create notification that will launch the provided intent when tapped
        val notification = buildNotification(
            context, "VPN certificate ready to install",
            "Tap to install certificate", installationIntent, false
        )
        showNotification(context, CERTIFICATE_INSTALLATION_NOTIFICATION_ID, notification)
    }

    override fun cancelInstallCertificateNotification(context: Context) {
        cancelNotification(context, CERTIFICATE_INSTALLATION_NOTIFICATION_ID)
    }

    override fun showAccessibilityPermissionNotification(context: Context) {
        // Create notification that will launch the main activity when tapped
        val pendingIntent = getPendingIntentForMainActivity(context, ENABLE_ACCESSIBILITY)
        val notification = buildNotification(
            context, "Enable accessibility",
            "Please enable accessibility", pendingIntent, false
        )
        showNotification(context, ACCESSIBILITY_NOTIFICATION_ID, notification)
    }

    override fun cancelAccessibilityPermissionNotification(context: Context) {
        cancelNotification(context, ACCESSIBILITY_NOTIFICATION_ID)
    }

    override fun showUsageStatsPermissionNotification(context: Context) {
        // Create notification that will launch the main activity when tapped
        val pendingIntent = getPendingIntentForMainActivity(context, ENABLE_USAGE_STATS)
        val notification = buildNotification(
            context, "Usage stats",
            "Please enable usage stats", pendingIntent, false
        )
        showNotification(context, USAGE_STATS_NOTIFICATION_ID, notification)
    }

    override fun cancelUsageStatsPermissionNotification(context: Context) {
        cancelNotification(context, USAGE_STATS_NOTIFICATION_ID)
    }

    override fun showAndroidPermissionsNotification(context: Context) {
        // Create notification that will launch the main activity when tapped
        val pendingIntent = getPendingIntentForMainActivity(context, GRANT_PERMISSIONS)
        val notification = buildNotification(
            context, "Permissions",
            "Please grant permissions", pendingIntent, false
        )
        showNotification(context, GRANT_PERMISSIONS_NOTIFICATION_ID, notification)
    }

    override fun cancelAndroidPermissionsNotification(context: Context) {
        cancelNotification(context, GRANT_PERMISSIONS_NOTIFICATION_ID)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannels(context: Context) {
        if (mMainChannel == null) {
            val mainChannelName = "Sample"
            mMainChannel = NotificationChannel(MAIN_CHANNEL_ID, mainChannelName, NotificationManager.IMPORTANCE_DEFAULT)
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mgr?.createNotificationChannel(mMainChannel)
        }
    }

    private fun buildNotification(
        context: Context, title: String, subtitle: String,
        pendingIntent: PendingIntent?, ongoing: Boolean
    ): Notification {
        val builder = NotificationCompat.Builder(context)
        builder.setContentTitle(title)
        builder.setContentText(subtitle)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setOnlyAlertOnce(true)
        if (ongoing) {
            builder.setOngoing(true)
        } else {
            builder.setOngoing(false)
            builder.setAutoCancel(true)
        }
        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // From Android 8 we must assign the notification to a channel
            createNotificationChannels(context)
            builder.setChannelId(MAIN_CHANNEL_ID)
        }
        return builder.build()
    }

    private fun showNotification(context: Context, notificationId: Int, notification: Notification) {
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr?.notify(notificationId, notification)
    }

    private fun cancelNotification(context: Context, notificationId: Int) {
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr?.cancel(notificationId)
    }

    private fun getPendingIntentForMainActivity(context: Context, action: String): PendingIntent {
        val activityIntent = Intent(context, MainActivity::class.java)
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        activityIntent.action = action
        return PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_CANCEL_CURRENT)
    }
}