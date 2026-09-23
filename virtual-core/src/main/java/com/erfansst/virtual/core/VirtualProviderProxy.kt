package com.erfansst.virtual.core
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import java.io.FileNotFoundException
class VirtualProviderProxy:ContentProvider(){
private val targets=HashMap<String,ContentProvider>()
private fun clone():CloneInfo?{
val a=info?.authority.orEmpty()
val id=a.removePrefix(VirtualProviderRegistry.PREFIX).toIntOrNull()?:return null
return CloneManager(requireContext()).list().firstOrNull{it.cloneId==id}
}
private fun target(uri:Uri):ContentProvider?{
val c=clone()?:return null
val auth=uri.authority?:return null
val k=c.packageName+"#"+c.cloneId+"#"+auth
return targets.getOrPut(k){VirtualProviderHost(requireContext()).get(c,auth)}
}
private fun real(uri:Uri)=uri.buildUpon().authority(originalAuthority(uri)).build()
private fun originalAuthority(uri:Uri)=uri.authority.orEmpty()
override fun onCreate()=true
override fun getType(uri:Uri):String?=target(uri)?.getType(real(uri))
override fun query(uri:Uri,p:Array<out String>?,s:String?,a:Array<String>?,sort:String?):Cursor?=target(uri)?.query(real(uri),p,s,a,sort)
override fun insert(uri:Uri,v:ContentValues?):Uri?=target(uri)?.insert(real(uri),v)
override fun delete(uri:Uri,s:String?,a:Array<String>?):Int=target(uri)?.delete(real(uri),s,a)?:0
override fun update(uri:Uri,v:ContentValues?,s:String?,a:Array<String>?):Int=target(uri)?.update(real(uri),v,s,a)?:0
override fun call(method:String,arg:String?,extras:Bundle?):Bundle?=runCatching{val c=clone()?:return null;val auth=extras?.getString("virtual_authority")?:return null;target(Uri.parse("content://"+auth))?.call(method,arg,extras)}.getOrNull()
override fun openFile(uri:Uri,mode:String):ParcelFileDescriptor?=target(uri)?.openFile(real(uri),mode)
@Throws(FileNotFoundException::class)
override fun openAssetFile(uri:Uri,mode:String)=target(uri)?.openAssetFile(real(uri),mode)
}