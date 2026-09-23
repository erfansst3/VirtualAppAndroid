package com.erfansst.virtual.core
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
class VirtualInstrumentation(private val base:Instrumentation,private val clone:CloneInfo,private val hostContext:Context):Instrumentation(){
private fun map(i:Intent):Intent{
val c=i.component
if(c!=null&&c.packageName==clone.packageName)return VirtualIntentDispatcher.activity(hostContext,clone,i)?:i
if(c==null&&i.getPackage()==clone.packageName&&!clone.launcherActivity.isNullOrBlank())return VirtualIntentDispatcher.activity(hostContext,clone,clone.launcherActivity!!)
return i
}
fun execStartActivity(who:Context,thread:IBinder,token:IBinder,target:Activity,intent:Intent,requestCode:Int,options:Bundle?):Instrumentation.ActivityResult?{
HiddenApi.relax()
return invoke("execStartActivity",arrayOf(Context::class.java,IBinder::class.java,IBinder::class.java,Activity::class.java,Intent::class.java,Int::class.javaPrimitiveType!!,Bundle::class.java),arrayOf(who,thread,token,target,map(intent),requestCode,options))
}
fun execStartActivities(who:Context,thread:IBinder,token:IBinder,target:Activity,intents:Array<Intent>,options:Bundle?){
HiddenApi.relax()
invokeVoid("execStartActivities",arrayOf(Context::class.java,IBinder::class.java,IBinder::class.java,Activity::class.java,Array<Intent>::class.java,Bundle::class.java),arrayOf(who,thread,token,target,intents.map{map(it)}.toTypedArray(),options))
}
private fun invoke(name:String,types:Array<Class<*>>,args:Array<Any?>):Instrumentation.ActivityResult?{
val m=Instrumentation::class.java.getDeclaredMethod(name,*types);m.isAccessible=true
return m.invoke(base,*args) as? Instrumentation.ActivityResult
}
private fun invokeVoid(name:String,types:Array<Class<*>>,args:Array<Any?>){
val m=Instrumentation::class.java.getDeclaredMethod(name,*types);m.isAccessible=true;m.invoke(base,*args)
}
}