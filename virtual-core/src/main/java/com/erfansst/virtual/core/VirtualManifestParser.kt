package com.erfansst.virtual.core
import android.content.IntentFilter
import android.content.res.AssetManager
import android.content.res.XmlResourceParser
import org.xmlpull.v1.XmlPullParser
object VirtualManifestParser{
data class ReceiverSpec(val name:String,val exported:Boolean,val filter:IntentFilter)
fun receivers(context:android.content.Context,clone:CloneInfo):List<ReceiverSpec>{
val amc=AssetManager::class.java.getDeclaredConstructor().apply{isAccessible=true}
val am=amc.newInstance()
val add=AssetManager::class.java.getDeclaredMethod("addAssetPath",String::class.java).apply{isAccessible=true}
val cookie=(add.invoke(am,clone.apk.path) as Number).toInt()
val open=AssetManager::class.java.getDeclaredMethod("openXmlResourceParser",Int::class.java,String::class.java).apply{isAccessible=true}
val x=open.invoke(am,cookie,"AndroidManifest.xml") as XmlResourceParser
val pkg=clone.packageName
val exported=context.packageManager.getPackageArchiveInfo(clone.apk.path,android.content.pm.PackageManager.GET_RECEIVERS)?.receivers.orEmpty().associateBy{it.name}
val out=ArrayList<ReceiverSpec>()
var current:String?=null
var filter:IntentFilter?=null
try{
while(true){
when(x.next()){
XmlPullParser.END_DOCUMENT->break
XmlPullParser.START_TAG->when(x.name){
"receiver"->current=x.attr("name")?.resolve(pkg)
"intent-filter"->if(current!=null){filter=IntentFilter();x.attr("priority")?.toIntOrNull()?.let{filter?.priority=it}}
"action"->x.attr("name")?.let{filter?.addAction(it)}
"category"->x.attr("name")?.let{filter?.addCategory(it)}
"data"->parseData(x,filter)
}
XmlPullParser.END_TAG->when(x.name){
"intent-filter"->if(current!=null&&filter!=null){out.add(ReceiverSpec(current!!,exported[current!!]?.exported?:false,filter!!));filter=null}
"receiver"->{current=null;filter=null}
}
}
}catch(_:Throwable){}finally{x.close()}
return out
}
private fun XmlResourceParser.attr(n:String)=getAttributeValue("http://schemas.android.com/apk/res/android",n)
private fun String.resolve(pkg:String)=if(startsWith("."))pkg+this else if(contains('.'))this else "$pkg.$this"
private fun parseData(x:XmlResourceParser,f:IntentFilter?){
val scheme=x.attr("scheme");if(scheme!=null)runCatching{f?.addDataScheme(scheme)}
val host=x.attr("host");val port=x.attr("port");if(host!=null)runCatching{f?.addDataAuthority(host,port)}
val mime=x.attr("mimeType");if(mime!=null)runCatching{f?.addDataType(mime)}
val path=x.attr("path");if(path!=null)runCatching{f?.addDataPath(path,0)}
val prefix=x.attr("pathPrefix");if(prefix!=null)runCatching{f?.addDataPath(prefix,1)}
val pattern=x.attr("pathPattern");if(pattern!=null)runCatching{f?.addDataPath(pattern,2)}
}
}