package com.erfansst.virtualapp
import android.app.Application
import com.erfansst.virtual.core.CrashReporter
import com.erfansst.virtual.core.VirtualApp
class VirtualHostApplication:Application(){
override fun onCreate(){super.onCreate();VirtualApp(this).init();CrashReporter.install(this)}
}