package com.erfansst.virtual.core
import android.content.Context
import java.io.File
class VirtualApp(private val context:Context){
val root:File by lazy{File(context.filesDir,"virtual")}
val appsDir:File by lazy{File(root,"apps")}
val logsDir:File by lazy{File(root,"logs")}
fun init(){appsDir.mkdirs();logsDir.mkdirs()}
}