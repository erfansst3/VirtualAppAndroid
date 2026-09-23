package com.erfansst.virtual.core
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class VirtualLogger(private val file:File){
private val fmt=SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",Locale.US)
@Synchronized fun log(level:String,tag:String,msg:String,t:Throwable?=null){
file.parentFile?.mkdirs()
val line=fmt.format(Date())+" "+level+"/"+tag+": "+msg+(if(t!=null){"\n"+Log.getStackTraceString(t)}else"")+"\n"
file.appendText(line)
Log.println(if(level=="E")Log.ERROR else if(level=="W")Log.WARN else Log.INFO,tag,msg)
}}