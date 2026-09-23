package com.erfansst.virtual.core
import android.content.res.AssetManager
import android.content.res.Resources
class VirtualResources{
fun open(base:Resources,paths:List<String>):Resources{
val a=AssetManager::class.java.getDeclaredConstructor()
a.isAccessible=true
val am=a.newInstance()
val add=AssetManager::class.java.getDeclaredMethod("addAssetPath",String::class.java)
add.isAccessible=true
paths.distinct().forEach{add.invoke(am,it)}
return Resources(am,base.displayMetrics,base.configuration)
}
}