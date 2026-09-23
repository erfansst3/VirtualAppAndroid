package com.erfansst.virtual.core
import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
object VirtualProxyComponents{
fun activity(id:Int):Class<out Activity> = when(id){
1->ProxyActivity::class.java
2->VirtualProxyActivity2::class.java
3->VirtualProxyActivity3::class.java
4->VirtualProxyActivity4::class.java
5->VirtualProxyActivity5::class.java
6->VirtualProxyActivity6::class.java
7->VirtualProxyActivity7::class.java
8->VirtualProxyActivity8::class.java
else->ProxyActivity::class.java
}
fun service(id:Int):Class<out Service> = when(id){
1->ProxyService::class.java
2->VirtualProxyService2::class.java
3->VirtualProxyService3::class.java
4->VirtualProxyService4::class.java
5->VirtualProxyService5::class.java
6->VirtualProxyService6::class.java
7->VirtualProxyService7::class.java
8->VirtualProxyService8::class.java
else->ProxyService::class.java
}
fun receiver(id:Int):Class<out BroadcastReceiver> = when(id){
1->ProxyReceiver::class.java
2->VirtualProxyReceiver2::class.java
3->VirtualProxyReceiver3::class.java
4->VirtualProxyReceiver4::class.java
5->VirtualProxyReceiver5::class.java
6->VirtualProxyReceiver6::class.java
7->VirtualProxyReceiver7::class.java
8->VirtualProxyReceiver8::class.java
else->ProxyReceiver::class.java
}
}