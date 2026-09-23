package com.erfansst.virtualapp
import android.app.Activity
import android.os.Bundle
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
root.addView(TextView(this).apply{text="Installed apps";textSize=18f;setPadding(0,8,0,12)})
list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};root.addView(list);setContentView(root);refresh()
}
private fun refresh(){
list.removeAllViews()
runtime.apps().forEach{item->
val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;setPadding(0,8,0,8)}
row.addView(TextView(this).apply{text=item.label+"\n"+item.packageName;textSize=16f},LinearLayout.LayoutParams(0,-2,1f))
row.addView(Button(this).apply{text="Clone";setOnClickListener{runCatching{runtime.clone(item)}.onSuccess{refresh()}.onFailure{toast(it.message?:"Clone failed")}}})
list.addView(row)
}
runtime.clones().forEach{clone->
val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;setPadding(0,16,0,8)}
row.addView(TextView(this).apply{text="Clone: "+clone.packageName+" #"+clone.cloneId;textSize=16f},LinearLayout.LayoutParams(0,-2,1f))
row.addView(Button(this).apply{text="Launch";setOnClickListener{
val i=runtime.launch(clone);if(i==null)toast("No launchable activity") else startActivity(i)
}})
row.addView(Button(this).apply{text="Delete";setOnClickListener{runtime.delete(clone.packageName,clone.cloneId);VirtualSessionManager.clear(clone.packageName,clone.cloneId);refresh()}})
list.addView(row)
}
}
private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}