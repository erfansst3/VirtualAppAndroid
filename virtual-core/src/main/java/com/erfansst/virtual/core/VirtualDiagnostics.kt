package com.erfansst.virtual.core
import android.content.Context
import java.io.File
object VirtualDiagnostics{
fun log(context:Context,message:String,t:Throwable?=null){
val s=VirtualSessionManager.current()
val text=if(t==null)message else message+" "+t.stackTraceToString()
VirtualLogStore.write(context,s?.clone,text)
}
fun read(context:Context,clone:CloneInfo):String{
val f=File(context.filesDir,"virtual/logs/"+clone.packageName+"_"+clone.cloneId+".log")
return if(f.exists())f.readText().takeLast(30000) else ""
}
}