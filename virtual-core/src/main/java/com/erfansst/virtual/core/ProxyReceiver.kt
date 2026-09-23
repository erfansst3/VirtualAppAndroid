package com.erfansst.virtual.core
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
class ProxyReceiver:BroadcastReceiver(){
override fun onReceive(context:Context,intent:Intent){
val p=intent.getStringExtra("clone_package")?:return
val id=intent.getIntExtra("clone_id",1)
val n=intent.getStringExtra("target_receiver")?:return
val c=CloneManager(context).list().firstOrNull{it.packageName==p&&it.cloneId==id}?:return
runCatching{
HiddenApi.relax()
val pi=context.packageManager.getPackageArchiveInfo(c.apk.path,android.content.pm.PackageManager.GET_RECEIVERS or android.content.pm.PackageManager.GET_META_DATA)?:return
val ai=android.content.pm.ApplicationInfo(pi.applicationInfo)
ai.packageName=c.packageName;ai.sourceDir=c.apk.path;ai.publicSourceDir=c.apk.path;ai.dataDir=c.dataDir.path;ai.nativeLibraryDir=FilePaths.lib(c).path
val cl=VirtualClassLoader(context).load(c)
val res=VirtualResources().open(context.resources,c.apk.path)
val vc=VirtualContext(context,c,ai,cl,res)
val appName=ai.className?.takeIf{it.isNotBlank()}?:android.app.Application::class.java.name
val app=cl.loadClass(appName).getDeclaredConstructor().newInstance() as android.app.Application
val am=android.app.Application::class.java.getDeclaredMethod("attach",Context::class.java);am.isAccessible=true;am.invoke(app,vc);app.onCreate()
val r=cl.loadClass(n).asSubclass(BroadcastReceiver::class.java).getDeclaredConstructor().newInstance()
r.onReceive(vc,Intent(intent))
}
}
}