package com.erfansst.virtual.core
import android.content.res.AssetManager
import android.content.res.Resources
class VirtualResources{
fun open(base:Resources,paths:List<String>):Resources{
val a=AssetManager()
paths.distinct().forEach{a.addAssetPath(it)}
return Resources(a,base.displayMetrics,base.configuration)
}
}