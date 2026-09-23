package com.erfansst.virtual.core
import android.content.Context
import dalvik.system.DexClassLoader
import java.io.File
class VirtualClassLoader(private val context:Context){
fun load(clone:CloneInfo):DexClassLoader{
val opt=File(clone.dataDir,"dex");opt.mkdirs()
val files=listOf(clone.apk)+clone.splits
files.forEach{it.setReadOnly()}
val lib=VirtualNativeLibs.extract(clone.apk,File(clone.dataDir,"lib"))
val paths=files.joinToString(File.pathSeparator){it.path}
return DexClassLoader(paths,opt.path,lib,context.classLoader)
}
}