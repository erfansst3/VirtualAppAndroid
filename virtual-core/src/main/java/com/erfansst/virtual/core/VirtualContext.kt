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
override fun getDataDir()=root
override fun getFilesDir()=File(root,"files").also{it.mkdirs()}
override fun getCacheDir()=File(root,"cache").also{it.mkdirs()}
override fun getCodeCacheDir()=File(root,"code_cache").also{it.mkdirs()}
override fun getNoBackupFilesDir()=File(root,"no_backup").also{it.mkdirs()}
override fun getDatabasePath(name:String)=File(root,"databases/$name").also{it.parentFile?.mkdirs()}
override fun getDir(name:String,mode:Int)=File(root,"app_$name").also{it.mkdirs()}
}