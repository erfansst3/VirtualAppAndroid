package com.erfansst.virtual.core
import android.content.res.AssetManager
import android.content.res.Resources
class VirtualResources{
fun open(base:Resources,apk:String):Resources{
val a=AssetManager()
a.addAssetPath(apk)
return Resources(a,base.displayMetrics,base.configuration)
}
}
