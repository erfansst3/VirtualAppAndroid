package com.erfansst.virtual.core
import android.app.Service
import android.content.Intent
import android.os.IBinder
class ProxyService:Service(){
private var target:Service?=null
override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int{
if(target==null){
val c=CloneManager(this).list().firstOrNull{it.packageName==intent?.getStringExtra("clone_package")&&it.cloneId==intent?.getIntExtra("clone_id",1)}
val n=intent?.getStringExtra("target_service")
if(c!=null&&!n.isNullOrBlank())runCatching{target=VirtualServiceHost(this).start(c,n)}
}
return target?.onStartCommand(Intent(intent),flags,startId)?:START_NOT_STICKY
}
override fun onBind(intent:Intent?):IBinder?=target?.onBind(intent)
override fun onDestroy(){runCatching{target?.onDestroy()};target=null;super.onDestroy()}
}