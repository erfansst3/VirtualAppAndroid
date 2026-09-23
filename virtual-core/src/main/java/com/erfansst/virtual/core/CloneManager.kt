package com.erfansst.virtual.core
import android.content.Context
import java.io.File
data class CloneInfo(val packageName:String,val cloneId:Int,val apk:File,val dataDir:File,val splits:List<File> = emptyList(),val launcherActivity:String? = null)
class CloneManager(private val context:Context){
private val root=File(context.filesDir,"virtual/clones")
private val store=CloneStore(root)
fun clone(app:InstalledApp,id:Int=nextId(app.packageName)):CloneInfo{
val dir=File(root,app.packageName+"_"+id);dir.mkdirs()
val apk=File(dir,"base.apk");File(app.appInfo.sourceDir).copyTo(apk,true)
val splitDir=File(dir,"splits");splitDir.mkdirs()
val splits=app.appInfo.splitSourceDirs.orEmpty().mapIndexed{n,p->File(splitDir,"split_$n.apk").also{File(p).copyTo(it,true)}}
val data=File(dir,"data");data.mkdirs()
val c=CloneInfo(app.packageName,id,apk,data,splits,app.launcherActivity)
val pi=context.packageManager.getPackageArchiveInfo(apk.path,0)
store.save(c,pi?.longVersionCode?:0L,pi?.versionName.orEmpty())
return c
}
fun list():List<CloneInfo>{
if(!root.exists())return emptyList()
return root.listFiles().orEmpty().mapNotNull{d->
val n=d.name.lastIndexOf('_');if(n<1)return@mapNotNull null
val id=d.name.substring(n+1).toIntOrNull()?:return@mapNotNull null
val p=d.name.substring(0,n);val apk=File(d,"base.apk")
if(!apk.exists())null else{
val m=store.load(d)
CloneInfo(p,id,apk,File(d,"data"),File(d,"splits").listFiles().orEmpty().toList(),m?.launcherActivity)
}}.sortedWith(compareBy({it.packageName},{it.cloneId}))
}
fun nextId(packageName:String):Int=(list().filter{it.packageName==packageName}.maxOfOrNull{it.cloneId}?:0)+1
fun delete(packageName:String,id:Int=1)=File(root,packageName+"_"+id).deleteRecursively()
}