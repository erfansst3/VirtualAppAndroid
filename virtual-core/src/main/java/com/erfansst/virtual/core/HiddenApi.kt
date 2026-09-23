package com.erfansst.virtual.core
import android.os.Build
object HiddenApi{
fun relax(){
if(Build.VERSION.SDK_INT<28)return
runCatching{
val c=Class.forName("dalvik.system.VMRuntime")
val r=c.getDeclaredMethod("getRuntime").invoke(null)
c.getDeclaredMethod("setHiddenApiExemptions",Array<String>::class.java).invoke(r,arrayOf("Landroid/","Lcom/android/internal/","Ldalvik/system/"))
}
}
}