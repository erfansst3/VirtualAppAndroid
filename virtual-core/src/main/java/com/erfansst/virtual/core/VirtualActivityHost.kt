package com.erfansst.virtual.core
import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.IBinder
import android.view.Window
import java.lang.reflect.Method
class VirtualActivityHost(private val host:Activity){
fun start(clone:CloneInfo,activityName:String):Activity{
HiddenApi.relax()
val pm=host.packageManager
val pi=pm.getPackageArchiveInfo(clone.apk.path,android.content.pm.PackageManager.GET_ACTIVITIES or android.content.pm.PackageManager.GET_META_DATA)?:error("APK parse failed")
val ai=ApplicationInfo(pi.applicationInfo)
ai.packageName=clone.packageName
ai.sourceDir=clone.apk.path
ai.publicSourceDir=clone.apk.path
ai.dataDir=clone.dataDir.path
ai.splitSourceDirs=clone.splits.map{it.path}.toTypedArray()
ai.nativeLibraryDir=FilePaths.lib(clone).path
val loader=VirtualClassLoader(host).load(clone)
val res=VirtualResources().open(host.resources,clone.apk.path)
val vc=VirtualContext(host,clone,ai,loader,res)
val appName=ai.className?.takeIf{it.isNotBlank()}?:Application::class.java.name
val app=loader.loadClass(appName).getDeclaredConstructor().newInstance() as Application
attachApplication(app,vc)
app.onCreate()
val info=pi.activities?.firstOrNull{it.name==activityName}?:error("Activity not found: $activityName")
info.applicationInfo=ai
info.packageName=clone.packageName
val target=loader.loadClass(activityName).asSubclass(Activity::class.java).getDeclaredConstructor().newInstance()
val inIntent=Intent(host.intent).setComponent(ComponentName(clone.packageName,activityName))
attachActivity(target,vc,app,inIntent,info,pm.getApplicationLabel(ai))
call(target,"onCreate",Bundle::class.java,null)
call(target,"onStart")
call(target,"onResume")
return target
}
private fun attachApplication(app:Application,ctx:Context){
val m=Application::class.java.getDeclaredMethod("attach",Context::class.java);m.isAccessible=true;m.invoke(app,ctx)
}
private fun attachActivity(a:Activity,ctx:Context,app:Application,intent:Intent,info:ActivityInfo,title:CharSequence){
val token=field(host,"mToken") as? IBinder
val main=field(host,"mMainThread")
val instr=field(host,"mInstrumentation") as? Instrumentation
val ident=(field(host,"mIdent") as? Int)?:0
val m=Activity::class.java.declaredMethods.filter{it.name=="attach"}.maxByOrNull{it.parameterTypes.size}?:error("Activity.attach unavailable")
m.isAccessible=true
var binder=false
val args=m.parameterTypes.map{t->
when{
t==Context::class.java->ctx
t.name=="android.app.ActivityThread"->main
t==Instrumentation::class.java->instr
t==IBinder::class.java->{if(!binder&&token!=null){binder=true;token}else null}
t==Int::class.javaPrimitiveType->ident
t==Application::class.java->app
t==Intent::class.java->intent
t==ActivityInfo::class.java->info
t==CharSequence::class.java->title
t==Activity::class.java->null
t==Configuration::class.java->Configuration(ctx.resources.configuration)
t==String::class.java->null
t.name.contains("NonConfigurationInstances")->null
t==Window::class.java->null
t.name.contains("IVoiceInteractor")->null
t.name.contains("ActivityConfigCallback")->null
else->null
}.toTypedArray()
m.invoke(a,*args)
}
private fun call(o:Any,name:String,vararg types:Class<*>,arg:Any?=null){
var c:Class<*>?=o.javaClass
while(c!=null){
runCatching{val m=c.getDeclaredMethod(name,*types);m.isAccessible=true;if(types.isEmpty())m.invoke(o)else m.invoke(o,arg);return}
c=c.superclass
}
}
private fun field(o:Any,n:String):Any?{
var c:Class<*>?=o.javaClass
while(c!=null){
try{val f=c.getDeclaredField(n);f.isAccessible=true;return f.get(o)}catch(_:Throwable){}
c=c.superclass
}
return null
}
}