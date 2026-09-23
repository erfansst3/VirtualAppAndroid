package com.erfansst.virtual.core
import android.content.Context
import android.content.pm.PackageManager
import java.io.File
data class VirtualApk(val packageName:String,val label:String,val versionName:String,val apk:File)
class ApkRepository(private val context:Context){
private val dir=File(context.filesDir,"virtual/apps")
fun import(apk:File):VirtualApk{
val pi=context.packageManager.getPackageArchiveInfo(apk.path,PackageManager.GET_META_DATA)?:error("Invalid APK")
val out=File(dir,pi.packageName+".apk");dir.mkdirs();apk.copyTo(out,true)
val ai=pi.applicationInfo?:error("Missing application info")
ai.sourceDir=out.path
ai.publicSourceDir=out.path
return VirtualApk(pi.packageName,context.packageManager.getApplicationLabel(ai).toString(),pi.versionName.orEmpty(),out)
}}