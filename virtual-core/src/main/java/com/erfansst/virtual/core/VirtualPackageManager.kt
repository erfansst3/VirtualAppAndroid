package com.erfansst.virtual.core
import android.content.Context
import android.content.Intent
class VirtualPackageManager(private val context:Context){
fun installed():List<InstalledApp>{
val pm=context.packageManager
val i=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
return pm.queryIntentActivities(i,0).map{r->
val a=r.activityInfo.applicationInfo
InstalledApp(a.packageName,pm.getApplicationLabel(a).toString(),pm.getPackageInfo(a.packageName,0).versionName.orEmpty(),a,r.activityInfo.name)
}.filter{it.packageName!=context.packageName}.distinctBy{it.packageName}.sortedBy{it.label.lowercase()}
}
}