package com.erfansst.virtual.core
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
class ProxyActivity:Activity(){
override fun onCreate(b:Bundle?){
super.onCreate(b)
val p=intent.getStringExtra("clone_package").orEmpty()
val a=intent.getStringExtra("target_activity").orEmpty()
setContentView(TextView(this).apply{text="Virtual process\n$p\n$a\n\nRuntime hook layer is required to attach the target Activity to Android framework services."})
}
}
