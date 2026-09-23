package com.erfansst.virtual.core
import android.content.Context
import java.io.File
data class CloneInfo(val packageName:String,val cloneId:Int,val apk:File,val dataDir:File,val splits:List<File>=emptyList(),val launcherActivity:String?=null)
class CloneManager(private val context:Context){
private val root=File(context.filesDir,"virtual/clones")
fun clone(app:InstalledApp,id:Int=1):CloneInfo{
val dir=File(root,app.packageName+"_"+id);dir.mkdirs()
val apk=File(dir,"base.apk");File(app.appInfo.sourceDir).copyTo(apk,true)
val splitDir=File(dir,"splits");splitDir.mkdirs()
val splits=app.appInfo.splitSourceDirs.orEmpty().mapIndexed{n,p->File(splitDir,"split_$n.apk").also{File(p).copyTo(it,true)}}
val data=File(dir,"data");data.mkdirs()
return CloneInfo(app.packageName,id,apk,data,splits,app.launcherActivity)
}
fun list():List<CloneInfo>{
if(!root.exists())return emptyList()
return root.listFiles().orEmpty().mapNotNull{d->
val n=d.name.lastIndexOf('_');if(n<1)return@mapNotNull null
val id=d.name.substring(n+1).toIntOrNull()?:return@mapNotNull null
val p=d.name.substring(0,n);val apk=File(d,"base.apk");if(!apk.exists())null else CloneInfo(p,id,apk,File(d,"data"),File(d,"splits").listFiles().orEmpty().toList())
}}
fun delete(packageName:String,id:Int=1)=File(root,packageName+"_"+id).deleteRecursively()
}