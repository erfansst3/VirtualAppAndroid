package com.erfansst.virtual.core
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import java.util.concurrent.ConcurrentHashMap
data class VirtualSession(val clone:CloneInfo,val appInfo:ApplicationInfo,val loader:ClassLoader,val context:VirtualContext,val application:Application)
object VirtualSessionManager{
private val sessions=ConcurrentHashMap<String,VirtualSession>()
private val current=object:InheritableThreadLocal<VirtualSession?>(){}
fun get(host:Context,clone:CloneInfo):VirtualSession{
val key=clone.packageName+"#"+clone.cloneId
sessions[key]?.let{return it}
synchronized(sessions){
sessions[key]?.let{return it}
HiddenApi.relax()
VirtualSystemHooks.install(host)
val pi=host.packageManager.getPackageArchiveInfo(clone.apk.path,PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES or PackageManager.GET_RECEIVERS or PackageManager.GET_PROVIDERS or PackageManager.GET_META_DATA)?:error("APK parse failed")
val ai=ApplicationInfo(pi.applicationInfo)
ai.packageName=clone.packageName;ai.sourceDir=clone.apk.path;ai.publicSourceDir=clone.apk.path;ai.dataDir=clone.dataDir.path;ai.splitSourceDirs=clone.splits.map{it.path}.toTypedArray();ai.nativeLibraryDir=FilePaths.lib(clone).path
val cl=VirtualClassLoader(host).load(clone)
val res=VirtualResources().open(host.resources,listOf(clone.apk.path)+clone.splits.map{it.path})
val ctx=VirtualContext(host,clone,ai,cl,res)
val name=ai.className?.takeIf{it.isNotBlank()}?:Application::class.java.name
val app=cl.loadClass(name).getDeclaredConstructor().newInstance() as Application
val attach=Application::class.java.getDeclaredMethod("attach",Context::class.java);attach.isAccessible=true;attach.invoke(app,ctx)
val s=VirtualSession(clone,ai,cl,ctx,app)
sessions[key]=s
activate(s)
VirtualBroadcastRegistrar.ensure(host,clone)
runCatching{app.onCreate()}.onFailure{sessions.remove(key);current.remove();throw it}
return s
}}
fun activate(s:VirtualSession){current.set(s)}
fun current():VirtualSession?=current.get()
fun clear(packageName:String,id:Int){val s=sessions.remove(packageName+"#"+id);s?.let{VirtualBroadcastRegistrar.clear(it.context.baseContext,it.clone)};if(current.get()?.clone?.packageName==packageName&&current.get()?.clone?.cloneId==id)current.remove()}
fun clear(){sessions.values.toList().forEach{VirtualBroadcastRegistrar.clear(it.context.baseContext,it.clone)};sessions.clear();current.remove()}
}