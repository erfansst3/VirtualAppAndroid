package com.erfansst.virtual.core
import android.content.Context
import java.io.File
object VirtualLogStore{
fun write(context:Context,clone:CloneInfo?,message:String){
val pkg=clone?.packageName?:"host"
val id=clone?.cloneId?:0
val f=File(context.filesDir,"virtual/logs/"+pkg+"_"+id+".log")
f.parentFile?.mkdirs()
f.appendText(System.currentTimeMillis().toString()+" "+message+"\n")
}
}