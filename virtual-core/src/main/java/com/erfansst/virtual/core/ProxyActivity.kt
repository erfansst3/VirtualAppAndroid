package com.erfansst.virtual.core
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
class ProxyActivity:Activity(){
private var target:Activity?=null
override fun onCreate(b:Bundle?){
super.onCreate(b)
val p=intent.getStringExtra("clone_package")?:return
val id=intent.getIntExtra("clone_id",1)
val a=intent.getStringExtra("target_activity")?:return
runCatching{
val c=CloneManager(this).list().firstOrNull{it.packageName==p&&it.cloneId==id}?:error("Clone missing")
target=VirtualActivityHost(this).start(c,a)
}.onFailure{Toast.makeText(this,"Clone failed: "+(it.message?:it.javaClass.simpleName),Toast.LENGTH_LONG).show();finish()}
}
override fun onStart(){super.onStart();runCatching{call("onStart")}}
override fun onResume(){super.onResume();runCatching{call("onResume")}}
override fun onPause(){runCatching{call("onPause")};super.onPause()}
override fun onStop(){runCatching{call("onStop")};super.onStop()}
override fun onDestroy(){runCatching{call("onDestroy")};VirtualSessionManager.clear(intent.getStringExtra("clone_package").orEmpty(),intent.getIntExtra("clone_id",1));super.onDestroy()}
override fun onNewIntent(i:Intent){super.onNewIntent(i);target?.let{call(it,"onNewIntent",Intent::class.java,i)}}
override fun onActivityResult(r:Int,c:Int,d:Intent?){super.onActivityResult(r,c,d);target?.let{call(it,"onActivityResult",Int::class.java,Int::class.java,Intent::class.java,r,c,d)}}
override fun onSaveInstanceState(out:Bundle){runCatching{target?.let{call(it,"onSaveInstanceState",Bundle::class.java,out)}};super.onSaveInstanceState(out)}
override fun onConfigurationChanged(c:android.content.res.Configuration){super.onConfigurationChanged(c);target?.let{call(it,"onConfigurationChanged",android.content.res.Configuration::class.java,c)}}
override fun onBackPressed(){runCatching{target?.let{call(it,"onBackPressed")}}.onFailure{finish()}}
private fun call(name:String,vararg args:Any?){
val t=target?:return
val types=args.map{it?.javaClass?:Any::class.java}.toTypedArray()
call(t,name,*types,*args)
}
private fun call(o:Any,name:String,vararg types:Class<*>,vararg args:Any?){
var c:Class<*>?=o.javaClass
while(c!=null){runCatching{val m=c.getDeclaredMethod(name,*types);m.isAccessible=true;m.invoke(o,*args);return};c=c.superclass}
}
}