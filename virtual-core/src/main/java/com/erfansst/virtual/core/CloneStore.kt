package com.erfansst.virtual.core
import java.io.File
import java.util.Properties
data class CloneMeta(val packageName:String,val cloneId:Int,val launcherActivity:String?,val versionCode:Long,val versionName:String)
class CloneStore(private val root:File){
private fun file(c:CloneInfo)=File(c.apk.parentFile,"clone.properties")
fun save(c:CloneInfo,versionCode:Long,versionName:String){
val p=Properties().apply{setProperty("package",c.packageName);setProperty("id",c.cloneId.toString());setProperty("launcher",c.launcherActivity.orEmpty());setProperty("versionCode",versionCode.toString());setProperty("versionName",versionName)}
file(c).outputStream().use{p.store(it,null)}
}
fun load(dir:File):CloneMeta?=runCatching{
val p=Properties();File(dir,"clone.properties").inputStream().use{p.load(it)}
CloneMeta(p.getProperty("package"),p.getProperty("id").toInt(),p.getProperty("launcher").orEmpty().ifBlank{null},p.getProperty("versionCode","0").toLong(),p.getProperty("versionName",""))
}.getOrNull()
}