package com.erfansst.virtual.core
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
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
private fun real(uri:Uri)=uri
override fun onCreate()=true
override fun getType(uri:Uri):String?=target(uri)?.getType(real(uri))
override fun query(uri:Uri,p:Array<out String>?,s:String?,a:Array<String>?,sort:String?):Cursor?=target(uri)?.query(real(uri),p,s,a,sort)
override fun insert(uri:Uri,v:ContentValues?):Uri?=target(uri)?.insert(real(uri),v)
override fun delete(uri:Uri,s:String?,a:Array<String>?):Int=target(uri)?.delete(real(uri),s,a)?:0
override fun update(uri:Uri,v:ContentValues?,s:String?,a:Array<String>?):Int=target(uri)?.update(real(uri),v,s,a)?:0
override fun openFile(uri:Uri,mode:String):ParcelFileDescriptor?=target(uri)?.openFile(real(uri),mode)
@Throws(FileNotFoundException::class)
override fun openAssetFile(uri:Uri,mode:String)=target(uri)?.openAssetFile(real(uri),mode)
override fun call(callingPkg:String?,attributionTag:String?,authority:String,method:String,arg:String?,extras:Bundle?):Bundle?=target(Uri.parse("content://"+authority))?.call(method,arg,extras)
override fun onDestroy(){targets.values.forEach{runCatching{it.shutdown()}};targets.clear();super.onDestroy()}
}