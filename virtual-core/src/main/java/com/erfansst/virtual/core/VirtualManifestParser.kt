package com.erfansst.virtual.core
import android.content.IntentFilter
import android.content.res.AssetManager
import android.content.res.XmlResourceParser
import org.xmlpull.v1.XmlPullParser
object VirtualManifestParser{
data class ReceiverSpec(val name:String,val exported:Boolean,val filter:IntentFilter)
fun receivers(context:android.content.Context,clone:CloneInfo):List<ReceiverSpec>{
val am=AssetManager::class.java.getDeclaredConstructor().apply{isAccessible=true}.newInstance()
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
val e=x.next()
if(e==XmlPullParser.END_DOCUMENT)break
if(e==XmlPullParser.START_TAG){
when(x.name){
"receiver"->{current=x.attr("name")?.resolve(pkg);filter=null}
"intent-filter"->{if(current!=null)filter=IntentFilter()}
"action"->{x.attr("name")?.let{filter?.addAction(it)}}
"category"->{x.attr("name")?.let{filter?.addCategory(it)}}
"data"->{data(x,filter)}
}
}else if(e==XmlPullParser.END_TAG){
if(x.name=="intent-filter"&&current!=null&&filter!=null)out.add(ReceiverSpec(current!!,exported[current!!]?.exported?:false,filter!!))
if(x.name=="receiver"){current=null;filter=null}
}
}
}catch(_:Throwable){}finally{x.close()}
return out
}
private fun XmlResourceParser.attr(n:String):String?=getAttributeValue("http://schemas.android.com/apk/res/android",n)
private fun String.resolve(pkg:String)=when{startsWith(".")->pkg+this;contains(".")->this;else->"$pkg.$this"}
private fun data(x:XmlResourceParser,f:IntentFilter?){
x.attr("scheme")?.let{runCatching{f?.addDataScheme(it)}}
x.attr("mimeType")?.let{runCatching{f?.addDataType(it)}}
x.attr("host")?.let{runCatching{f?.addDataAuthority(it,x.attr("port"))}}
}
}