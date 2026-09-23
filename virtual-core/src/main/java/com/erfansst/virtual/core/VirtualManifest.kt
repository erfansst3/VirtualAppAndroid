package com.erfansst.virtual.core
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
class VirtualManifest(private val pm:PackageManager){
fun packageInfo(apk:String)=pm.getPackageArchiveInfo(apk,PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES or PackageManager.GET_RECEIVERS or PackageManager.GET_PROVIDERS or PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS)
fun activities(apk:String):List<VirtualComponent>{
val p=packageInfo(apk)?:return emptyList()
return p.activities.orEmpty().map{VirtualComponent(it.name,0,it.exported,it.permission,it.processName)}
}
fun launcher(apk:String):VirtualComponent?{
val p=packageInfo(apk)?:return null
return p.activities.orEmpty().firstOrNull{it.exported&&it.name.isNotBlank()&&it.intentFiltersSafe()}
}
private fun ActivityInfo.intentFiltersSafe():Boolean=true
}