package com.erfansst.virtual.core
import android.app.Application
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
class VirtualServiceHost(private val proxy:Service){
fun start(clone:CloneInfo,name:String):Service{
HiddenApi.relax()
val pi=proxy.packageManager.getPackageArchiveInfo(clone.apk.path,PackageManager.GET_SERVICES or PackageManager.GET_META_DATA)?:error("APK parse failed")
val ai=android.content.pm.ApplicationInfo(pi.applicationInfo)
ai.packageName=clone.packageName;ai.sourceDir=clone.apk.path;ai.publicSourceDir=clone.apk.path;ai.dataDir=clone.dataDir.path;ai.nativeLibraryDir=FilePaths.lib(clone).path
val loader=VirtualClassLoader(proxy).load(clone)
val res=VirtualResources().open(proxy.resources,listOf(clone.apk.path)+clone.splits.map{it.path})
val ctx=VirtualContext(proxy,clone,ai,loader,res)
val appName=ai.className?.takeIf{it.isNotBlank()}?:Application::class.java.name
val app=loader.loadClass(appName).getDeclaredConstructor().newInstance() as Application
val am=field(proxy,"mActivityManager")
attachApplication(app,ctx)
app.onCreate()
val s=loader.loadClass(name).asSubclass(Service::class.java).getDeclaredConstructor().newInstance()
val token=field(proxy,"mToken") as? IBinder
val thread=field(proxy,"mThread")
val m=Service::class.java.getDeclaredMethod("attach",Context::class.java,Class.forName("android.app.ActivityThread"),String::class.java,IBinder::class.java,Application::class.java,Object::class.java)
m.isAccessible=true;m.invoke(s,ctx,thread,name,token,app,am)
s.onCreate()
return s
}
private fun attachApplication(a:Application,c:android.content.Context){val m=Application::class.java.getDeclaredMethod("attach",Context::class.java);m.isAccessible=true;m.invoke(a,c)}
private fun field(o:Any,n:String):Any?{
var c:Class<*>?=o.javaClass
while(c!=null){try{val f=c.getDeclaredField(n);f.isAccessible=true;return f.get(o)}catch(_:Throwable){};c=c.superclass}
return null
}
}