package com.erfansst.virtual.core
import android.content.Context
import android.content.Intent
class VirtualLauncher(private val context:Context){
fun intent(clone:CloneInfo):Intent?{
val name=clone.launcherActivity?:return null
return Intent(context,VirtualProxyComponents.activity(clone.cloneId)).putExtra("clone_package",clone.packageName).putExtra("clone_id",clone.cloneId).putExtra("target_activity",name)
}
}