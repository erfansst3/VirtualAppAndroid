package com.erfansst.virtual.core
import android.os.Build
import java.io.File
import java.util.zip.ZipFile
object VirtualNativeLibs{
fun extract(apk:File,out:File):String{
out.mkdirs()
val wanted=Build.SUPPORTED_ABIS.toList()
ZipFile(apk).use{z->
z.entries().asSequence().filter{!it.isDirectory&&it.name.startsWith("lib/")&&it.name.endsWith(".so")}.sortedBy{e->
val abi=e.name.split('/').getOrNull(1).orEmpty();wanted.indexOf(abi).let{if(it<0)999 else it}
}.forEach{e->
val abi=e.name.split('/').getOrNull(1).orEmpty()
if(abi in wanted){
val f=File(out,e.name.substringAfterLast('/'))
if(!f.exists())z.getInputStream(e).use{input->f.outputStream().use{input.copyTo(it)}}
}}
}
return out.path
}
}