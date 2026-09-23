package com.erfansst.virtual.core
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
class ProxyService:Service(){
private val targets=HashMap<String,Service>()
private fun target(intent:Intent?):Service?{
val p=intent?.getStringExtra("clone_package")?:return null
val id=intent.getIntExtra("clone_id",1)
val n=intent.getStringExtra("target_service")?:return null
val c=CloneManager(this).list().firstOrNull{it.packageName==p&&it.cloneId==id}?:return null
return targets.getOrPut(p+"#"+id+"#"+n){VirtualServiceHost(this).start(c,n)}
}
private fun clean(i:Intent):Intent{
val p=i.getStringExtra("clone_package")
val n=i.getStringExtra("target_service")
return Intent(i).apply{removeExtra("clone_package");removeExtra("clone_id");removeExtra("target_service");if(!p.isNullOrBlank()&&!n.isNullOrBlank())component=ComponentName(p,n)}
}
override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int{
val i=intent?:return START_NOT_STICKY
val t=runCatching{target(i)}.getOrNull()?:return START_NOT_STICKY
return t.onStartCommand(clean(i),flags,startId)
}
override fun onBind(intent:Intent?):IBinder?{
val i=intent?:return null
return runCatching{target(i)?.onBind(clean(i))}.getOrNull()
}
override fun onDestroy(){targets.values.toList().forEach{runCatching{it.onDestroy()}};targets.clear();super.onDestroy()}
}