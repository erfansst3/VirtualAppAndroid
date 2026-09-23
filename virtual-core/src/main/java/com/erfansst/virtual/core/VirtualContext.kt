package com.erfansst.virtual.core
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ApplicationInfo
import android.content.res.Resources
import java.io.File
class VirtualContext(base:Context,val clone:CloneInfo,val appInfo:ApplicationInfo,val loader:ClassLoader,res:Resources):ContextWrapper(base){
private val root=clone.dataDir
private val vr=res
override fun getPackageName()=appInfo.packageName
override fun getClassLoader()=loader
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
override fun checkSelfPermission(permission:String)=baseContext.packageManager.checkPermission(permission,appInfo.packageName)
override fun checkCallingOrSelfPermission(permission:String)=checkSelfPermission(permission)
override fun checkCallingPermission(permission:String)=checkSelfPermission(permission)
}