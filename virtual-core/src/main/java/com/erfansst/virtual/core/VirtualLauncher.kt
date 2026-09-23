package com.erfansst.virtual.core
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
class VirtualLauncher(private val context:Context){
fun launcherActivity(clone:CloneInfo):String?{
val p=context.packageManager.getPackageArchiveInfo(clone.apk.path,PackageManager.GET_ACTIVITIES)
return p?.activities?.firstOrNull{a->a.exported&&a.name.isNotBlank()}?.name
}
fun intent(clone:CloneInfo):Intent?{
val name=launcherActivity(clone)?:return null
return Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setClassName(context.packageName,ProxyActivity::class.java.name).putExtra("clone_package",clone.packageName).putExtra("clone_id",clone.cloneId).putExtra("target_activity",name)
}
}
