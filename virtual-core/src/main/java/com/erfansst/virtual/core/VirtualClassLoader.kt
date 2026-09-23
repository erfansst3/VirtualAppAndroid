package com.erfansst.virtual.core
import android.content.Context
import dalvik.system.DexClassLoader
import java.io.File
class VirtualClassLoader(private val context:Context){
fun load(clone:CloneInfo):DexClassLoader{
val opt=File(clone.dataDir,"dex");opt.mkdirs()
val lib=File(clone.dataDir,"lib");lib.mkdirs()
return DexClassLoader(clone.apk.path,opt.path,lib.path,context.classLoader)
}
}
