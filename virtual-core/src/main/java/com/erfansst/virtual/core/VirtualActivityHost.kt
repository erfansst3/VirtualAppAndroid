package com.erfansst.virtual.core
import android.app.Activity
import android.app.Instrumentation
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.view.Window
import android.content.res.Configuration
class VirtualActivityHost(private val host:Activity){
fun start(clone:CloneInfo,activityName:String):Activity{
val s=VirtualSessionManager.get(host,clone)
VirtualSessionManager.activate(s)
val info=(host.packageManager.getPackageArchiveInfo(clone.apk.path,PackageManager.GET_ACTIVITIES)?.activities.orEmpty().firstOrNull{it.name==activityName})?:error("Activity not found: $activityName")
info.applicationInfo=s.appInfo;info.packageName=clone.packageName
val target=s.loader.loadClass(activityName).asSubclass(Activity::class.java).getDeclaredConstructor().newInstance()
val i=Intent(host.intent).apply{component=ComponentName(clone.packageName,activityName);removeExtra("clone_package");removeExtra("clone_id");removeExtra("target_activity")}
attach(target,s.context,s.application,i,info,host.packageManager.getApplicationLabel(s.appInfo),clone)
info.getThemeResource().takeIf{it!=0}?.let{target.setTheme(it)}
call(target,"onCreate",arrayOf(Bundle::class.java),arrayOf(null))
val decor=target.window?.decorView?:error("Target window unavailable")
host.setContentView(decor)
return target
}
private fun attach(a:Activity,ctx:VirtualContext,app:android.app.Application,intent:Intent,info:ActivityInfo,title:CharSequence,clone:CloneInfo){
val token=field(host,"mToken") as? IBinder
val main=field(host,"mMainThread")
val instr0=field(host,"mInstrumentation") as? Instrumentation
val instr=instr0?.let{VirtualInstrumentation(it,clone,host)}
val ident=(field(host,"mIdent") as? Int)?:0
val m=Activity::class.java.declaredMethods.firstOrNull{it.name=="attach"}?:error("Activity.attach unavailable")
m.isAccessible=true
val args=m.parameterTypes.map{t->when{
t==android.content.Context::class.java->ctx
t.name=="android.app.ActivityThread"->main
t==Instrumentation::class.java->instr
t==IBinder::class.java->token
t==Int::class.javaPrimitiveType->ident
t==android.app.Application::class.java->app
t==Intent::class.java->intent
t==ActivityInfo::class.java->info
t==CharSequence::class.java->title
t==Activity::class.java->null
t==Configuration::class.java->Configuration(ctx.resources.configuration)
t==String::class.java->null
t.name.contains("NonConfigurationInstances")->null
t.name.contains("IVoiceInteractor")->null
else->null
}}.toTypedArray()
m.invoke(a,*args)
}
private fun call(o:Any,name:String,types:Array<Class<*>>,args:Array<Any?>){
var c:Class<*>?=o.javaClass
while(c!=null){
runCatching{val m=c.getDeclaredMethod(name,*types);m.isAccessible=true;m.invoke(o,*args);return}
c=c.superclass
}
}
private fun field(o:Any,n:String):Any?{
var c:Class<*>?=o.javaClass
while(c!=null){try{val f=c.getDeclaredField(n);f.isAccessible=true;return f.get(o)}catch(_:Throwable){};c=c.superclass}
return null
}
}