package com.erfansst.virtual.core
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.*
import android.os.Process
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicBoolean
object VirtualSystemHooks{
private val installed=AtomicBoolean()
fun install(context:Context){
if(!installed.compareAndSet(false,true))return
HiddenApi.relax()
runCatching{hookSingleton("android.app.ActivityTaskManager","IActivityTaskManagerSingleton","android.app.IActivityTaskManager")}
runCatching{hookSingleton("android.app.ActivityManager","IActivityManagerSingleton","android.app.IActivityManager")}
runCatching{hookPackageManager(context)}
}
private fun hookSingleton(owner:String,field:String,ifaceName:String){
val c=Class.forName(owner);val sf=c.getDeclaredField(field);sf.isAccessible=true
val singleton=sf.get(null);val f=singleton.javaClass.getDeclaredField("mInstance");f.isAccessible=true
val base=f.get(singleton)?:return
val iface=Class.forName(ifaceName)
val p=Proxy.newProxyInstance(iface.classLoader,arrayOf(iface),handler(base))
f.set(singleton,p)
}
private fun hookPackageManager(context:Context){
val tc=Class.forName("android.app.ActivityThread");val f=tc.getDeclaredField("sPackageManager");f.isAccessible=true
val base=f.get(null)?:return;val iface=Class.forName("android.content.pm.IPackageManager")
val p=Proxy.newProxyInstance(iface.classLoader,arrayOf(iface),handler(base));f.set(null,p)
runCatching{
val pm=context.packageManager;val m=pm.javaClass.getDeclaredField("mPM");m.isAccessible=true;m.set(pm,p)
}
}
private fun handler(base:Any)=InvocationHandler{_,m,args->
val a=args?:emptyArray()
val s=VirtualSessionManager.current()
if(s!=null){
val rewritten=a.map{arg->
when(arg){
is Intent->rewrite(arg,s,m.name)
is Array<*>->if(arg.isArrayOf<Intent>())arg.map{rewrite(it as Intent,s,m.name)}.toTypedArray()else arg
else->arg
}}.toTypedArray()
if(m.name.startsWith("getApplicationInfo")||m.name.startsWith("getPackageInfo")||m.name.startsWith("getApplicationInfoAsUser")||m.name.startsWith("getPackageInfoAsUser"))return@InvocationHandler spoof(m.invoke(base,*rewritten),s)
if(m.name=="getPackageUid"&&a.firstOrNull()==s.appInfo.packageName)return@InvocationHandler Process.myUid()
return@InvocationHandler m.invoke(base,*rewritten)
}
m.invoke(base,*a)
}
private fun rewrite(i:Intent,s:VirtualSession,name:String):Intent{
val c=i.component?:return i
if(c.packageName!=s.appInfo.packageName)return i
return when{
name.contains("service",true)->VirtualIntentDispatcher.service(s.context.baseContext,s.clone,i)?:i
name.contains("broadcast",true)->VirtualIntentDispatcher.receiver(s.context.baseContext,s.clone,i)?:i
else->VirtualIntentDispatcher.activity(s.context.baseContext,s.clone,i)?:i
}
}
private fun spoof(x:Any?,s:VirtualSession):Any?=when(x){
is ApplicationInfo->ApplicationInfo(x).apply{packageName=s.appInfo.packageName;uid=Process.myUid();dataDir=s.appInfo.dataDir;sourceDir=s.appInfo.sourceDir;publicSourceDir=s.appInfo.publicSourceDir;nativeLibraryDir=s.appInfo.nativeLibraryDir;splitSourceDirs=s.appInfo.splitSourceDirs}
is PackageInfo->PackageInfo(x).apply{packageName=s.appInfo.packageName;applicationInfo=spoof(applicationInfo,s) as ApplicationInfo}
is ActivityInfo->ActivityInfo(x).apply{applicationInfo=spoof(applicationInfo,s) as ApplicationInfo}
is ServiceInfo->ServiceInfo(x).apply{applicationInfo=spoof(applicationInfo,s) as ApplicationInfo}
is ProviderInfo->ProviderInfo(x).apply{applicationInfo=spoof(applicationInfo,s) as ApplicationInfo}
is ResolveInfo->ResolveInfo(x).apply{activityInfo=activityInfo?.let{spoof(it,s) as ActivityInfo};serviceInfo=serviceInfo?.let{spoof(it,s) as ServiceInfo};providerInfo=providerInfo?.let{spoof(it,s) as ProviderInfo}}
is List<*>->x.map{spoof(it,s)}
else->x
}
}