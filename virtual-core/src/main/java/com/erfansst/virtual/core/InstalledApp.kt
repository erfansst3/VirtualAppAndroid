package com.erfansst.virtual.core
import android.content.pm.ApplicationInfo
data class InstalledApp(val packageName:String,val label:String,val versionName:String,val appInfo:ApplicationInfo,val launcherActivity:String)