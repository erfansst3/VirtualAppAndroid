package com.erfansst.virtual.core
import android.content.Context
class VirtualRuntime(context:Context){
private val pm=VirtualPackageManager(context)
private val clones=CloneManager(context)
fun apps():List<InstalledApp> = pm.installed()
fun clone(app:InstalledApp,id:Int=1):CloneInfo = clones.clone(app,id)
fun clones():List<CloneInfo> = clones.list()
}