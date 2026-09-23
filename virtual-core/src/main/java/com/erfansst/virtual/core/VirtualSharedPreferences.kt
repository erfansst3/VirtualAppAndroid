package com.erfansst.virtual.core
import android.content.SharedPreferences
import org.json.JSONArray
import java.io.File
import java.util.Properties
import java.util.concurrent.CopyOnWriteArraySet
class VirtualSharedPreferences(private val file:File):SharedPreferences{
private val values=LinkedHashMap<String,Any?>()
private val listeners=CopyOnWriteArraySet<SharedPreferences.OnSharedPreferenceChangeListener>()
init{load()}
private fun load(){
if(!file.exists())return
runCatching{
val p=Properties();file.inputStream().use{p.load(it)}
p.stringPropertyNames().forEach{k->
val v=p.getProperty(k)
values[k]=when(v.firstOrNull()){
's'->v.substring(1)
'i'->v.substring(1).toIntOrNull()?:0
'l'->v.substring(1).toLongOrNull()?:0L
'f'->v.substring(1).toFloatOrNull()?:0f
'b'->v.substring(1)=="1"
'a'->JSONArray(v.substring(1)).let{j->LinkedHashSet<String>().also{set->for(i in 0 until j.length())set.add(j.getString(i))}}
else->v
}
}
}}
private fun save(){
file.parentFile?.mkdirs()
val p=Properties()
values.forEach{(k,v)->p[k]=when(v){
is String->"s$v"
is Int->"i$v"
is Long->"l$v"
is Float->"f$v"
is Boolean->"b"+if(v)"1" else "0"
is Set<*>->"a"+JSONArray(v.toList()).toString()
else->"s$v"
}}
file.outputStream().use{p.store(it,"")}
}
override fun getAll():Map<String,*> = HashMap(values)
override fun getString(k:String,d:String?):String?=values[k] as? String?:d
override fun getStringSet(k:String,d:Set<String>?):Set<String>?=(values[k] as? Set<*>)?.filterIsInstance<String>()?.toSet()?:d
override fun getInt(k:String,d:Int)=values[k] as? Int?:d
override fun getLong(k:String,d:Long)=values[k] as? Long?:d
override fun getFloat(k:String,d:Float)=values[k] as? Float?:d
override fun getBoolean(k:String,d:Boolean)=values[k] as? Boolean?:d
override fun contains(k:String)=values.containsKey(k)
override fun edit():SharedPreferences.Editor=E()
override fun registerOnSharedPreferenceChangeListener(l:SharedPreferences.OnSharedPreferenceChangeListener){listeners.add(l)}
override fun unregisterOnSharedPreferenceChangeListener(l:SharedPreferences.OnSharedPreferenceChangeListener){listeners.remove(l)}
private inner class E:SharedPreferences.Editor{
private val changes=LinkedHashMap<String,Any?>()
private var clear=false
override fun putString(k:String,v:String?)=apply{changes[k]=v}
override fun putStringSet(k:String,v:Set<String>?)=apply{changes[k]=v?.toSet()}
override fun putInt(k:String,v:Int)=apply{changes[k]=v}
override fun putLong(k:String,v:Long)=apply{changes[k]=v}
override fun putFloat(k:String,v:Float)=apply{changes[k]=v}
override fun putBoolean(k:String,v:Boolean)=apply{changes[k]=v}
override fun remove(k:String)=apply{changes[k]=this@VirtualSharedPreferences}
override fun clear()=apply{clear=true}
override fun commit():Boolean{applyChanges();return true}
override fun apply(){applyChanges()}
private fun applyChanges(){
if(clear)values.clear()
val changed=changes.keys.toList()
changes.forEach{(k,v)->if(v===this@VirtualSharedPreferences)values.remove(k)else if(v!=null)values[k]=v}
save();changed.forEach{listeners.forEach{l->l.onSharedPreferenceChanged(this@VirtualSharedPreferences,it)}}
}
}
}