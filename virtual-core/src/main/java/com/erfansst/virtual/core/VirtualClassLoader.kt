package com.erfansst.virtual.core
import android.content.Context
import dalvik.system.DexClassLoader
import java.io.File
class VirtualClassLoader(private val context:Context){
fun load(clone:CloneInfo):DexClassLoader{
val opt=File(clone.dataDir,"dex");opt.mkdirs()
val lib=VirtualNativeLibs.extract(clone.apk,File(clone.dataDir,"lib"))
val paths=(listOf(clone.apk.path)+clone.splits.map{it.path}).joinToString(File.pathSeparator)
return DexClassLoader(paths,opt.path,lib,context.classLoader)
}
}