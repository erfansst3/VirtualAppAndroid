package com.erfansst.virtual.core
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
object VirtualIntentDispatcher{
private const val P="clone_package"
private const val I="clone_id"
private const val A="target_activity"
private const val S="target_service"
private const val R="target_receiver"
fun activity(base:Context,clone:CloneInfo,intent:Intent):Intent?{
val name=resolveActivity(base,clone,intent)?:return null
if(intent.component?.packageName!=clone.packageName&&intent.getPackage()!=clone.packageName&&intent.component!=null)return null
return Intent(intent).setComponent(ComponentName(base,ProxyActivity::class.java)).putExtra(P,clone.packageName).putExtra(I,clone.cloneId).putExtra(A,name)
}
fun service(base:Context,clone:CloneInfo,intent:Intent):Intent?{
val name=resolveService(base,clone,intent)?:return null
if(intent.component?.packageName!=clone.packageName&&intent.getPackage()!=clone.packageName&&intent.component!=null)return null
return Intent(intent).setComponent(ComponentName(base,ProxyService::class.java)).putExtra(P,clone.packageName).putExtra(I,clone.cloneId).putExtra(S,name)
}
fun receiver(base:Context,clone:CloneInfo,name:String,intent:Intent):Intent=Intent(intent).setComponent(ComponentName(base,ProxyReceiver::class.java)).putExtra(P,clone.packageName).putExtra(I,clone.cloneId).putExtra(R,name)
fun receiver(base:Context,clone:CloneInfo,intent:Intent):Intent?{
val name=resolveReceiver(base,clone,intent)?:return null
if(intent.component?.packageName!=clone.packageName&&intent.getPackage()!=clone.packageName&&intent.component!=null)return null
return Intent(intent).setComponent(ComponentName(base,ProxyReceiver::class.java)).putExtra(P,clone.packageName).putExtra(I,clone.cloneId).putExtra(R,name)
}
fun activity(base:Context,clone:CloneInfo,name:String):Intent=Intent(base,ProxyActivity::class.java).putExtra(P,clone.packageName).putExtra(I,clone.cloneId).putExtra(A,name)
fun service(base:Context,clone:CloneInfo,name:String):Intent=Intent(base,ProxyService::class.java).putExtra(P,clone.packageName).putExtra(I,clone.cloneId).putExtra(S,name)
fun receiver(base:Context,clone:CloneInfo,name:String):Intent=Intent(base,ProxyReceiver::class.java).putExtra(P,clone.packageName).putExtra(I,clone.cloneId).putExtra(R,name)
private fun scoped(clone:CloneInfo,intent:Intent)=Intent(intent).apply{setPackage(clone.packageName);component?.let{if(it.packageName!=clone.packageName)component=null}}
private fun resolveActivity(base:Context,clone:CloneInfo,intent:Intent):String?{
intent.component?.takeIf{it.packageName==clone.packageName}?.className?.let{return it}
val q=base.packageManager.queryIntentActivities(scoped(clone,intent),PackageManager.MATCH_ALL)
return q.firstOrNull{it.activityInfo.packageName==clone.packageName}?.activityInfo?.name
}
private fun resolveService(base:Context,clone:CloneInfo,intent:Intent):String?{
intent.component?.takeIf{it.packageName==clone.packageName}?.className?.let{return it}
val q=base.packageManager.queryIntentServices(scoped(clone,intent),PackageManager.MATCH_ALL)
return q.firstOrNull{it.serviceInfo.packageName==clone.packageName}?.serviceInfo?.name
}
private fun resolveReceiver(base:Context,clone:CloneInfo,intent:Intent):String?{
intent.component?.takeIf{it.packageName==clone.packageName}?.className?.let{return it}
val q=base.packageManager.queryBroadcastReceivers(scoped(clone,intent),PackageManager.MATCH_ALL)
return q.firstOrNull{it.activityInfo?.packageName==clone.packageName}?.activityInfo?.name
}
}