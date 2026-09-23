package com.erfansst.virtual.core
import android.content.Context
object GmsCompatibility{
private val packages=listOf("com.google.android.gms","com.google.android.gsf","com.android.vending")
fun installed(context:Context):List<String>{
return packages.filter{runCatching{context.packageManager.getPackageInfo(it,0)}.isSuccess}
}
}