package com.erfansst.virtual.core
import android.content.ComponentName
import android.content.Context
import android.content.Intent
object VirtualIntentDispatcher{
private const val P="clone_package"
private const val I="clone_id"
private const val A="target_activity"
private const val S="target_service"
private const val R="target_receiver"
fun activity(base:Context,clone:CloneInfo,intent:Intent):Intent?{
val c=intent.component?:return null
if(c.packageName!=clone.packageName)return null
return Intent(intent).setComponent(ComponentName(base,ProxyActivity::class.java)).putExtra(P,clone.packageName).putExtra(I,clone.cloneId).putExtra(A,c.className)
}
fun service(base:Context,clone:CloneInfo,intent:Intent):Intent?{
val c=intent.component?:return null
if(c.packageName!=clone.packageName)return null
return Intent(intent).setComponent(ComponentName(base,ProxyService::class.java)).putExtra(P,clone.packageName).putExtra(I,clone.cloneId).putExtra(S,c.className)
}
fun receiver(base:Context,clone:CloneInfo,intent:Intent):Intent?{
val c=intent.component?:return null
if(c.packageName!=clone.packageName)return null
return Intent(intent).setComponent(ComponentName(base,ProxyReceiver::class.java)).putExtra(P,clone.packageName).putExtra(I,clone.cloneId).putExtra(R,c.className)
}
fun activity(base:Context,clone:CloneInfo,name:String):Intent=Intent(base,ProxyActivity::class.java).putExtra(P,clone.packageName).putExtra(I,clone.cloneId).putExtra(A,name)
fun service(base:Context,clone:CloneInfo,name:String):Intent=Intent(base,ProxyService::class.java).putExtra(P,clone.packageName).putExtra(I,clone.cloneId).putExtra(S,name)
fun receiver(base:Context,clone:CloneInfo,name:String):Intent=Intent(base,ProxyReceiver::class.java).putExtra(P,clone.packageName).putExtra(I,clone.cloneId).putExtra(R,name)
}