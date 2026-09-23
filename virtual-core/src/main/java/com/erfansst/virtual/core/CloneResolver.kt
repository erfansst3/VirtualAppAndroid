package com.erfansst.virtual.core
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import java.io.File
class CloneResolver(private val context:Context){
fun manifest(clone:CloneInfo):CloneManifest{
val p=context.packageManager.getPackageArchiveInfo(clone.apk.path,PackageManager.GET_ACTIVITIES)
val main=p?.activities?.firstOrNull{a->Intent(context,Any::class.java).component==null&&a.name.isNotBlank()}?.name
return CloneManifest(clone.packageName,p?.longVersionCode?:0L,p?.versionName.orEmpty(),main,File(clone.apk.parentFile!!,"splits").listFiles().orEmpty().map{it.path})
}
}
