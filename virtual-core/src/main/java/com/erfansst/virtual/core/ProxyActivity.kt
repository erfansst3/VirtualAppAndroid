package com.erfansst.virtual.core
import android.app.Activity
import android.os.Bundle
import android.widget.Toast
class ProxyActivity:Activity(){
private var target:Activity?=null
override fun onCreate(b:Bundle?){
super.onCreate(b)
val p=intent.getStringExtra("clone_package")?:return
val id=intent.getIntExtra("clone_id",1)
val a=intent.getStringExtra("target_activity")?:return
runCatching{target=VirtualActivityHost(this).start(CloneManager(this).list().firstOrNull{it.packageName==p&&it.cloneId==id}?:error("Clone missing"),a)}.onFailure{
Toast.makeText(this,"Clone failed: "+(it.message?:it.javaClass.simpleName),Toast.LENGTH_LONG).show()
finish()
}
}
override fun onBackPressed(){
runCatching{target?.let{it.javaClass.getMethod("onBackPressed").invoke(it)}}.onFailure{finish()}
}
override fun onDestroy(){
runCatching{target?.let{it.javaClass.getDeclaredMethod("onDestroy").apply{isAccessible=true}.invoke(it)}};super.onDestroy()
}
}