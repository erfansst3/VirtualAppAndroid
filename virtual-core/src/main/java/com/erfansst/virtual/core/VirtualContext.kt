package com.erfansst.virtual.core
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.ComponentName
import android.content.pm.ApplicationInfo
import android.content.res.Resources
import android.os.Handler
import android.os.Bundle
import android.os.IBinder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
class VirtualContext(base:Context,val clone:CloneInfo,val appInfo:ApplicationInfo,val loader:ClassLoader,res:Resources):ContextWrapper(base){
private val root=clone.dataDir
private val vr=res
private val receivers=HashMap<BroadcastReceiver,BroadcastReceiver>()
private val connections=HashMap<ServiceConnection,ServiceConnection>()
private val prefs=HashMap<String,VirtualSharedPreferences>()
private var resolver:android.content.ContentResolver?=null
override fun getPackageName()=appInfo.packageName
override fun getOpPackageName()=appInfo.packageName
override fun getBasePackageName()=appInfo.packageName
override fun getClassLoader()=loader
override fun getAssets()=vr.assets
override fun createPackageContext(packageName:String,flags:Int):Context=if(packageName==appInfo.packageName)this else super.createPackageContext(packageName,flags)
override fun getResources()=vr
override fun getApplicationInfo()=appInfo
override fun getApplicationContext():Context=this
override fun getPackageCodePath()=clone.apk.path
override fun getPackageResourcePath()=clone.apk.path
override fun getDataDir()=root
override fun getFilesDir()=File(root,"files").also{it.mkdirs()}
override fun getCacheDir()=File(root,"cache").also{it.mkdirs()}
override fun getCodeCacheDir()=File(root,"code_cache").also{it.mkdirs()}
override fun getNoBackupFilesDir()=File(root,"no_backup").also{it.mkdirs()}
override fun getExternalFilesDir(type:String?)=File(root,"external_files"+(if(type==null)"-root" else "/$type")).also{it.mkdirs()}
override fun getExternalCacheDir()=File(root,"external_cache").also{it.mkdirs()}
override fun getDatabasePath(name:String)=File(root,"databases/$name").also{it.parentFile?.mkdirs()}
override fun getDir(name:String,mode:Int)=File(root,"app_$name").also{it.mkdirs()}
override fun getSharedPreferences(name:String,mode:Int)=prefs.getOrPut(name){VirtualSharedPreferences(File(root,"shared_prefs/$name.properties"))}
override fun getContentResolver():android.content.ContentResolver{resolver?.let{return it};val thread=field(baseContext,"mMainThread")?:field(baseContext,"mActivityThread")?:error("ActivityThread unavailable");val c=Class.forName("android.app.ContextImpl\$ApplicationContentResolver");val ctor=c.getDeclaredConstructor(Context::class.java,Class.forName("android.app.ActivityThread"));ctor.isAccessible=true;return ctor.newInstance(this,thread).also{resolver=it as android.content.ContentResolver}}
private fun field(o:Any,n:String):Any?{var c:Class<*>?=o.javaClass;while(c!=null){try{val f=c.getDeclaredField(n);f.isAccessible=true;return f.get(o)}catch(_:Throwable){};c=c.superclass};return null}
override fun getFileStreamPath(name:String)=File(getFilesDir(),name)
override fun openFileInput(name:String)=FileInputStream(getFileStreamPath(name))
override fun openFileOutput(name:String,mode:Int)=FileOutputStream(getFileStreamPath(name),mode and MODE_APPEND!=0)
override fun deleteFile(name:String)=getFileStreamPath(name).delete()
override fun fileList()=getFilesDir().list().orEmpty()
override fun checkSelfPermission(permission:String)=baseContext.packageManager.checkPermission(permission,appInfo.packageName)
override fun checkCallingOrSelfPermission(permission:String)=checkSelfPermission(permission)
override fun checkCallingPermission(permission:String)=checkSelfPermission(permission)
override fun startActivity(intent:Intent){
val i=VirtualIntentDispatcher.activity(baseContext,clone,intent)
if(i!=null)baseContext.startActivity(i)else baseContext.startActivity(intent)
}
override fun startActivity(intent:Intent,options:Bundle?){
val i=VirtualIntentDispatcher.activity(baseContext,clone,intent)
if(i!=null)baseContext.startActivity(i,options)else baseContext.startActivity(intent,options)
}
override fun startService(service:Intent):ComponentName?{
val i=VirtualIntentDispatcher.service(baseContext,clone,service)
return baseContext.startService(i?:service)
}
override fun startForegroundService(service:Intent):ComponentName?{
val i=VirtualIntentDispatcher.service(baseContext,clone,service)
return baseContext.startForegroundService(i?:service)
}
override fun stopService(service:Intent):Boolean{
val i=VirtualIntentDispatcher.service(baseContext,clone,service)
return baseContext.stopService(i?:service)
}
override fun bindService(service:Intent,conn:ServiceConnection,flags:Int):Boolean{
val i=VirtualIntentDispatcher.service(baseContext,clone,service)?:service
if(i===service)return baseContext.bindService(service,conn,flags)
val w=object:ServiceConnection{
override fun onServiceConnected(n:ComponentName,b:IBinder){conn.onServiceConnected(ComponentName(clone.packageName,service.component?.className?:n.className),b)}
override fun onServiceDisconnected(n:ComponentName){conn.onServiceDisconnected(ComponentName(clone.packageName,service.component?.className?:n.className))}
}
connections[conn]=w
return baseContext.bindService(i,w,flags)
}
override fun unbindService(conn:ServiceConnection){baseContext.unbindService(connections.remove(conn)?:conn)}
override fun sendBroadcast(intent:Intent){baseContext.sendBroadcast(VirtualIntentDispatcher.receiver(baseContext,clone,intent)?:intent)}
override fun sendBroadcast(intent:Intent,receiverPermission:String?){baseContext.sendBroadcast(VirtualIntentDispatcher.receiver(baseContext,clone,intent)?:intent,receiverPermission)}
override fun registerReceiver(receiver:BroadcastReceiver?,filter:IntentFilter):Intent?{
val r=receiver?:return baseContext.registerReceiver(null,filter)
val w=object:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){r.onReceive(this@VirtualContext,i)}}
receivers[r]=w
return baseContext.registerReceiver(w,filter)
}
override fun registerReceiver(receiver:BroadcastReceiver?,filter:IntentFilter,flags:Int):Intent?{
val r=receiver?:return baseContext.registerReceiver(null,filter,flags)
val w=object:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){r.onReceive(this@VirtualContext,i)}}
receivers[r]=w
return baseContext.registerReceiver(w,filter,flags)
}
override fun registerReceiver(receiver:BroadcastReceiver?,filter:IntentFilter,permission:String?,scheduler:Handler?):Intent?{
val r=receiver?:return baseContext.registerReceiver(null,filter,permission,scheduler)
val w=object:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){r.onReceive(this@VirtualContext,i)}}
receivers[r]=w
return baseContext.registerReceiver(w,filter,permission,scheduler)
}
override fun registerReceiver(receiver:BroadcastReceiver?,filter:IntentFilter,permission:String?,scheduler:Handler?,flags:Int):Intent?{
val r=receiver?:return baseContext.registerReceiver(null,filter,permission,scheduler,flags)
val w=object:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){r.onReceive(this@VirtualContext,i)}}
receivers[r]=w
return baseContext.registerReceiver(w,filter,permission,scheduler,flags)
}
override fun unregisterReceiver(receiver:BroadcastReceiver){baseContext.unregisterReceiver(receivers.remove(receiver)?:receiver)}
}