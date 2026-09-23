package com.erfansst.virtualapp
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import com.erfansst.virtual.core.*
import java.io.File
class MainActivity:Activity(){
private lateinit var app:VirtualApp
private lateinit var repo:ApkRepository
private lateinit var list:LinearLayout
override fun onCreate(b:Bundle?){
super.onCreate(b);app=VirtualApp(this).also{it.init()};CrashReporter.install(this);repo=ApkRepository(this)
val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,24,24,24)}
val title=TextView(this).apply{text="VirtualAppAndroid";textSize=24f}
val add=Button(this).apply{text="Import APK"};val logs=Button(this).apply{text="View Logs"}
list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
root.addView(title);root.addView(add);root.addView(logs);root.addView(list);setContentView(root)
add.setOnClickListener{startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="application/vnd.android.package-archive";addCategory(Intent.CATEGORY_OPENABLE)},10)}
logs.setOnClickListener{showLogs()}
}
override fun onActivityResult(r:Int,c:Int,d:Intent?){super.onActivityResult(r,c,d);if(r==10&&c==RESULT_OK)d?.data?.let{uri->
val f=File(cacheDir,"import.apk");contentResolver.openInputStream(uri)?.use{input->f.outputStream().use{input.copyTo(it)}}
runCatching{repo.import(f)}.onSuccess{addRow(it)}.onFailure{e->Toast.makeText(this,e.message,Toast.LENGTH_LONG).show()}}}
private fun addRow(v:VirtualApk){list.addView(TextView(this).apply{text=v.label+"\n"+v.packageName+" • "+v.versionName;textSize=16f;setPadding(12,12,12,12)})}
private fun showLogs(){val f=File(app.logsDir,"host.log");val text=if(f.exists())f.readText() else "No logs";AlertDialog.Builder(this).setTitle("Logs").setMessage(text.takeLast(12000)).setPositiveButton("OK",null).show()}
}