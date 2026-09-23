package com.erfansst.virtual.core
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
abstract class VirtualActivityProxyBase(private val slot:Int):Activity(){
private var target:Activity?=null
override fun onCreate(b:Bundle?){
super.onCreate(b)
val p=intent.getStringExtra("clone_package")?:return
val id=intent.getIntExtra("clone_id",slot)
val a=intent.getStringExtra("target_activity")?:return
if(id!=slot){finish();return}
runCatching{
val c=CloneManager(this).list().firstOrNull{it.packageName==p&&it.cloneId==id}?:error("Clone missing")
target=VirtualActivityHost(this).start(c,a)
}.onFailure{VirtualDiagnostics.log(this,"Activity start failed",it);Toast.makeText(this,"Clone failed: "+(it.message?:it.javaClass.simpleName),Toast.LENGTH_LONG).show();finish()}
}
override fun onStart(){super.onStart();call("onStart")}
override fun onResume(){super.onResume();call("onResume")}
override fun onPause(){call("onPause");super.onPause()}
override fun onStop(){call("onStop");super.onStop()}
override fun onDestroy(){call("onDestroy");super.onDestroy()}
override fun onNewIntent(i:Intent){super.onNewIntent(i);call("onNewIntent",arrayOf(Intent::class.java),arrayOf(i))}
override fun onActivityResult(r:Int,c:Int,d:Intent?){super.onActivityResult(r,c,d);call("onActivityResult",arrayOf(Int::class.java,Int::class.java,Intent::class.java),arrayOf(r,c,d))}
override fun onSaveInstanceState(out:Bundle){call("onSaveInstanceState",arrayOf(Bundle::class.java),arrayOf(out));super.onSaveInstanceState(out)}
override fun onConfigurationChanged(c:android.content.res.Configuration){super.onConfigurationChanged(c);call("onConfigurationChanged",arrayOf(android.content.res.Configuration::class.java),arrayOf(c))}
override fun onBackPressed(){runCatching{call("onBackPressed")}.onFailure{finish()}}
private fun call(name:String,types:Array<Class<*>> = emptyArray(),args:Array<Any?> = emptyArray()){
val t=target?:return
var c:Class<*>?=t.javaClass
while(c!=null){runCatching{val m=c.getDeclaredMethod(name,*types);m.isAccessible=true;m.invoke(t,*args);return};c=c.superclass}
}
}