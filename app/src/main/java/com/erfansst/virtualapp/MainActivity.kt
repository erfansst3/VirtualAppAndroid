package com.erfansst.virtualapp
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import com.erfansst.virtual.core.*
class MainActivity:Activity(){
private lateinit var app:VirtualApp
private lateinit var runtime:VirtualRuntime
private lateinit var list:LinearLayout
override fun onCreate(b:Bundle?){
super.onCreate(b)
app=VirtualApp(this).also{it.init()}
CrashReporter.install(this)
runtime=VirtualRuntime(this)
val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,24,24,24)}
root.addView(TextView(this).apply{text="VirtualAppAndroid";textSize=24f})
root.addView(TextView(this).apply{text="Installed apps";textSize=18f;setPadding(0,24,0,12)})
list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
root.addView(list)
setContentView(root)
refresh()
}
private fun refresh(){
list.removeAllViews()
runtime.apps().forEach{item->
val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;setPadding(0,8,0,8)}
val info=TextView(this).apply{text=item.label+"\n"+item.packageName;textSize=16f}
row.addView(info,LinearLayout.LayoutParams(0,-2,1f))
row.addView(Button(this).apply{text="Clone";setOnClickListener{
runCatching{runtime.clone(item)}.onSuccess{showStatus(item,"Clone prepared")}.onFailure{showStatus(item,it.message?:"Clone failed")}
}})
list.addView(row)
}
}
private fun showStatus(item:InstalledApp,msg:String){
AlertDialog.Builder(this).setTitle(item.label).setMessage(msg+"\n\nThe APK is taken directly from the installed app. No APK import is required.\n\nFull in-process virtualization still requires Android framework hooks before the cloned app can be launched inside this process.").setPositiveButton("OK",null).show()
}
}