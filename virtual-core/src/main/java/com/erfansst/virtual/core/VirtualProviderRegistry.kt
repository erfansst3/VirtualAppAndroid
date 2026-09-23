package com.erfansst.virtual.core
import android.content.pm.PackageManager
object VirtualProviderRegistry{
const val PREFIX="com.erfansst.virtualapp.virtualprovider"
const val MAX_SLOTS=8
fun stubAuthority(id:Int)=PREFIX+id
fun targetProvider(context:android.content.Context,clone:CloneInfo,authority:String):String?{
val p=context.packageManager.getPackageArchiveInfo(clone.apk.path,PackageManager.GET_PROVIDERS)?:return null
return p.providers.orEmpty().firstOrNull{it.authority?.split(';')?.contains(authority)==true}?.name
}
fun isTarget(context:android.content.Context,clone:CloneInfo,authority:String)=targetProvider(context,clone,authority)!=null
}