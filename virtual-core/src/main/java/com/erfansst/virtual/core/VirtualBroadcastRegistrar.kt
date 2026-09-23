package com.erfansst.virtual.core
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.concurrent.ConcurrentHashMap
object VirtualBroadcastRegistrar{
private val registrations=ConcurrentHashMap<String,List<Pair<BroadcastReceiver,VirtualManifestParser.ReceiverSpec>>>()
fun ensure(context:Context,clone:CloneInfo){
val key=clone.packageName+"#"+clone.cloneId
if(registrations.containsKey(key))return
val list=ArrayList<Pair<BroadcastReceiver,VirtualManifestParser.ReceiverSpec>>()
VirtualManifestParser.receivers(context,clone).forEach{spec->
val r=object:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){dispatch(context,clone,spec.name,i)}}
if(Build.VERSION.SDK_INT>=33){
val flags=if(spec.exported)Context.RECEIVER_EXPORTED else Context.RECEIVER_NOT_EXPORTED
context.registerReceiver(r,spec.filter,flags)
}else context.registerReceiver(r,spec.filter)
list.add(r to spec)
}
registrations[key]=list
}
private fun dispatch(context:Context,clone:CloneInfo,name:String,intent:Intent){
runCatching{
val s=VirtualSessionManager.get(context,clone)
VirtualSessionManager.activate(s)
val r=s.loader.loadClass(name).asSubclass(BroadcastReceiver::class.java).getDeclaredConstructor().newInstance()
val i=Intent(intent).apply{component=null;setPackage(null)}
r.onReceive(s.context,i)
}.onFailure{VirtualDiagnostics.log(context,"Receiver failed: "+name,it)}
}
fun clear(context:Context,clone:CloneInfo){
registrations.remove(clone.packageName+"#"+clone.cloneId)?.forEach{runCatching{context.unregisterReceiver(it.first)}}
}
}