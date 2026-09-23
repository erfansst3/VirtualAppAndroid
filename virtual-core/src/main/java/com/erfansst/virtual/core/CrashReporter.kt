package com.erfansst.virtual.core
import android.content.Context
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
object CrashReporter{
fun install(context:Context){
val f=File(context.filesDir,"virtual/logs/host-crash.log")
val old=Thread.getDefaultUncaughtExceptionHandler()
Thread.setDefaultUncaughtExceptionHandler{t,e->
f.parentFile?.mkdirs()
val d=SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",Locale.US).format(Date())
val s=VirtualSessionManager.current()
val line=d+" "+Build.MODEL+" "+Build.VERSION.SDK_INT+" "+t+" clone="+(s?.clone?.packageName?:"host")+"#"+(s?.clone?.cloneId?:0)+"\n"+e.stackTraceToString()+"\n"
f.appendText(line)
s?.let{VirtualLogStore.write(context,it.clone,"CRASH "+e.stackTraceToString())}
old?.uncaughtException(t,e)
}}}