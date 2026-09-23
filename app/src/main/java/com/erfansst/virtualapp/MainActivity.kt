package com.erfansst.virtualapp
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import com.erfansst.virtual.core.*
class MainActivity:Activity(){
private lateinit var runtime:VirtualRuntime
private lateinit var list:LinearLayout
override fun onCreate(b:Bundle?){
super.onCreate(b);runtime=VirtualRuntime(this)
val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,24,24,24)}
root.addView(TextView(this).apply{text="VirtualAppAndroid";textSize=24f})
root.addView(TextView(this).apply{text="GMS: "+if(GmsCompatibility.installed(this@MainActivity).isEmpty())"not detected" else "detected";textSize=15f;setPadding(0,8,0,16)})
val scroll=ScrollView(this)
list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
scroll.addView(list,ViewGroup.LayoutParams(-1,-2))
root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
setContentView(root);refresh()
}
private fun refresh(){
list.removeAllViews()
list.addView(TextView(this).apply{text="Installed apps";textSize=18f;setPadding(0,4,0,12)})
runtime.apps().forEach{item->
val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;setPadding(0,8,0,8)}
row.addView(TextView(this).apply{text=item.label+"\n"+item.packageName;textSize=16f},LinearLayout.LayoutParams(0,-2,1f))
row.addView(Button(this).apply{text="Clone";setOnClickListener{isEnabled=false;text="...";Thread{
runCatching{runtime.clone(item)}.onSuccess{c->runOnUiThread{refresh();toast("Clone #"+c.cloneId+" created");runtime.launch(c)?.let{startActivity(it)}}}.onFailure{e->runOnUiThread{refresh();toast("Clone failed: "+(e.message?:e.javaClass.simpleName))}}
}.start()}})
list.addView(row)
}
list.addView(TextView(this).apply{text="Your clones";textSize=18f;setPadding(0,20,0,12)})
runtime.clones().forEach{clone->
val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;setPadding(0,8,0,8)}
row.addView(TextView(this).apply{text=clone.packageName+"  #"+clone.cloneId;textSize=16f},LinearLayout.LayoutParams(0,-2,1f))
row.addView(Button(this).apply{text="Launch";setOnClickListener{runtime.launch(clone)?.let{runCatching{startActivity(it)}.onFailure{toast("Launch failed: "+(it.message?:it.javaClass.simpleName))}}?:toast("No launchable activity")}})
row.addView(Button(this).apply{text="Logs";setOnClickListener{val t=VirtualDiagnostics.read(this@MainActivity,clone);AlertDialog.Builder(this@MainActivity).setTitle(clone.packageName+" #"+clone.cloneId).setMessage(if(t.isEmpty())"No logs" else t).setPositiveButton("OK",null).show()}})
row.addView(Button(this).apply{text="Delete";setOnClickListener{runtime.delete(clone.packageName,clone.cloneId);VirtualSessionManager.clear(clone.packageName,clone.cloneId);refresh()}})
list.addView(row)
}
}
private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}