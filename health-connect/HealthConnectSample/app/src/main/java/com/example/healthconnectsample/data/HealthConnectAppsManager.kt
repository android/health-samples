package com.example.healthconnectsample.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ResolveInfoFlags
import android.content.res.Resources.NotFoundException
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES

class HealthConnectAppsManager(private val context: Context) {

    val healthConnectCompatibleApps by lazy {
        val intent = Intent("androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE")

        val packages = if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                intent,
                ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            context.packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_ALL
            )
        }

        packages.associate {
            val icon = try {
                context.packageManager.getApplicationIcon(it.activityInfo.packageName)
            } catch (e: NotFoundException) {
                null
            }
            val label = context.packageManager.getApplicationLabel(it.activityInfo.applicationInfo)
                .toString()
            it.activityInfo.packageName to
                HealthConnectAppInfo(
                    packageName = it.activityInfo.packageName,
                    icon = icon,
                    appLabel = label
                )
        }
    }
}
