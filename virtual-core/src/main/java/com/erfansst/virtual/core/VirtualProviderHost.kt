package com.erfansst.virtual.core
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.content.ContentProvider
class VirtualProviderHost(private val context:Context){
fun get(clone:CloneInfo,authority:String):ContentProvider{
val s=VirtualSessionManager.get(context,clone)
val p=context.packageManager.getPackageArchiveInfo(clone.apk.path,PackageManager.GET_PROVIDERS)?:error("APK parse failed")
val info=p.providers.orEmpty().firstOrNull{it.authority?.split(';')?.contains(authority)==true}?:error("Provider not found: $authority")
info.applicationInfo=s.appInfo;info.packageName=clone.packageName;info.authority=authority
return s.application.let{
val c=s.loader.loadClass(info.name).asSubclass(ContentProvider::class.java)
c.getDeclaredConstructor().newInstance().also{provider->provider.attachInfo(s.context,info)}
}
}
}