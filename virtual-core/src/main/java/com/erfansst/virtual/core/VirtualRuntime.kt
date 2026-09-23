package com.erfansst.virtual.core
import android.content.Context
import android.content.Intent
class VirtualRuntime(private val context:Context){
private val pm=VirtualPackageManager(context)
private val clones=CloneManager(context)
fun apps():List<InstalledApp>=pm.installed()
fun clone(app:InstalledApp,id:Int=1):CloneInfo=clones.clone(app,id)
fun clones():List<CloneInfo>=clones.list()
fun delete(packageName:String,id:Int=1)=clones.delete(packageName,id)
fun launch(clone:CloneInfo):Intent?=VirtualLauncher(context).intent(clone)
}