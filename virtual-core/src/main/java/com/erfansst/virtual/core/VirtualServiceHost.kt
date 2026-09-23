package com.erfansst.virtual.core
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
class VirtualServiceHost(private val proxy:Service){
fun start(clone:CloneInfo,name:String):Service{
val s=VirtualSessionManager.get(proxy,clone)
VirtualSessionManager.activate(s)
val a=s.loader.loadClass(name).asSubclass(Service::class.java).getDeclaredConstructor().newInstance()
val token=field(proxy,"mToken") as? IBinder
val thread=field(proxy,"mThread")
val am=field(proxy,"mActivityManager")
val m=Service::class.java.getDeclaredMethod("attach",Context::class.java,Class.forName("android.app.ActivityThread"),String::class.java,IBinder::class.java,android.app.Application::class.java,Object::class.java)
m.isAccessible=true;m.invoke(a,s.context,thread,name,token,s.application,am)
a.onCreate()
return a
}
private fun field(o:Any,n:String):Any?{
var c:Class<*>?=o.javaClass
while(c!=null){try{val f=c.getDeclaredField(n);f.isAccessible=true;return f.get(o)}catch(_:Throwable){};c=c.superclass}
return null
}
}