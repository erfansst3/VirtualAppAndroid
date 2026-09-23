package com.erfansst.virtual.core
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
class VirtualComponentResolver(private val context:android.content.Context){
fun activity(clone:CloneInfo,intent:Intent):String?{
val c=intent.component
if(c!=null&&c.packageName==clone.packageName)return c.className
val a=intent.getStringExtra("target_activity")
if(!a.isNullOrBlank())return a
return clone.launcherActivity
}
fun service(clone:CloneInfo,intent:Intent):String?=intent.component?.takeIf{it.packageName==clone.packageName}?.className
fun receiver(clone:CloneInfo,intent:Intent):String?=intent.component?.takeIf{it.packageName==clone.packageName}?.className
fun provider(clone:CloneInfo,authority:String):String?{
val p=context.packageManager.getPackageArchiveInfo(clone.apk.path,PackageManager.GET_PROVIDERS)?:return null
return p.providers?.firstOrNull{it.authority?.split(';')?.contains(authority)==true}?.name
}
fun target(clone:CloneInfo,name:String)=ComponentName(clone.packageName,name)
}