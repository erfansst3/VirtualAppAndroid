package com.erfansst.virtual.core
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
abstract class VirtualReceiverProxyBase(private val slot:Int):BroadcastReceiver(){
override fun onReceive(context:Context,intent:Intent){
val p=intent.getStringExtra("clone_package")?:return
val id=intent.getIntExtra("clone_id",slot)
val n=intent.getStringExtra("target_receiver")?:return
if(id!=slot)return
val c=CloneManager(context).list().firstOrNull{it.packageName==p&&it.cloneId==id}?:return
runCatching{
val s=VirtualSessionManager.get(context,c)
VirtualSessionManager.activate(s)
val r=s.loader.loadClass(n).asSubclass(BroadcastReceiver::class.java).getDeclaredConstructor().newInstance()
val i=Intent(intent).apply{removeExtra("clone_package");removeExtra("clone_id");removeExtra("target_receiver");component=android.content.ComponentName(c.packageName,n)}
r.onReceive(s.context,i)
}.onFailure{VirtualDiagnostics.log(context,"Receiver failed",it)}
}
}