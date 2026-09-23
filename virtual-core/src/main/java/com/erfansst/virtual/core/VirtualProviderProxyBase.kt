package com.erfansst.virtual.core
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.content.res.AssetFileDescriptor
import java.io.FileNotFoundException
abstract class VirtualProviderProxyBase(private val cloneId:Int):ContentProvider(){
private val targets=HashMap<String,ContentProvider>()
private fun clone():CloneInfo?=context?.let{CloneManager(it).list().firstOrNull{c->c.cloneId==cloneId}}
private fun target(uri:Uri):ContentProvider?{
val c=clone()?:return null
val a=uri.authority?:return null
if(!VirtualProviderRegistry.isTarget(requireNotNull(context),c,a))return null
val k=c.packageName+"#"+c.cloneId+"#"+a
return targets.getOrPut(k){VirtualProviderHost(requireNotNull(context)).get(c,a)}
}
override fun onCreate()=true
override fun getType(uri:Uri):String?=target(uri)?.getType(uri)
override fun query(uri:Uri,p:Array<out String>?,s:String?,a:Array<String>?,sort:String?):Cursor?=target(uri)?.query(uri,p,s,a,sort)
override fun insert(uri:Uri,v:ContentValues?):Uri?=target(uri)?.insert(uri,v)
override fun delete(uri:Uri,s:String?,a:Array<String>?):Int=target(uri)?.delete(uri,s,a)?:0
override fun update(uri:Uri,v:ContentValues?,s:String?,a:Array<String>?):Int=target(uri)?.update(uri,v,s,a)?:0
override fun openFile(uri:Uri,mode:String):ParcelFileDescriptor?=target(uri)?.openFile(uri,mode)
@Throws(FileNotFoundException::class)
override fun openAssetFile(uri:Uri,mode:String):AssetFileDescriptor?=target(uri)?.openAssetFile(uri,mode)
override fun call(method:String,arg:String?,extras:Bundle?):Bundle?=null
override fun call(authority:String,method:String,arg:String?,extras:Bundle?):Bundle?=target(Uri.parse("content://"+authority))?.call(method,arg,extras)
override fun shutdown(){targets.values.forEach{runCatching{it.shutdown()}};targets.clear();super.shutdown()}
}