@file:Suppress("DEPRECATION")

package com.example.automoto

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.SupplicantState
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.test.withTestContext
import java.time.LocalDateTime


@Suppress("DEPRECATION")
open class MainActivity : AppCompatActivity() {

     var status:Int = 0
     val TAG:String="MainActivity"

    var timer_to_run = 0;
    lateinit var mainHandler: Handler



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val Switch_Track:LinearLayout = findViewById(R.id.switchtrack)
        val Switch_Round:LinearLayout = findViewById(R.id.switchround)
        val BlueBack:LinearLayout = findViewById(R.id.blueback)
        val pumpname : TextView = findViewById(R.id.Pump_Name)
        pumpname.text = "AutoMoto Motor"

        Switch_Status()
        Get_Wifi_Strength()
        Get_Wifi_Name()

        Switch_Track.setOnClickListener {
            Switch_Status()
        }

        Switch_Round.setOnClickListener{
            Switch_Status()
        }

    }


        @RequiresApi(Build.VERSION_CODES.O)
        var current = LocalDateTime.now();

        var To_Run = object : Thread() {override fun run() {}}

     @RequiresApi(Build.VERSION_CODES.O)
     fun Switch_Status()
     {
         val Switch_Track:LinearLayout = findViewById(R.id.switchtrack)
         val Switch_Round:LinearLayout = findViewById(R.id.switchround)
         val BlueBack:LinearLayout = findViewById(R.id.blueback)
         val layoutParams = Switch_Round.layoutParams as LinearLayout.LayoutParams
         if(status == 0)
         {
             layoutParams.topMargin = 90.toDp(this)
             Switch_Round.setBackgroundResource(R.drawable.switchoff)
             BlueBack.setBackgroundResource(R.drawable.round_borders_off)
             status = 1

             try {
                 timer_to_run = 0;


                 val timer: TextView = findViewById(R.id.Timer)
                 timer.text = "00:00"

             }
             catch (Ex:java.lang.Exception)
             {
                 Toast.makeText(this, "error is $Ex", Toast.LENGTH_SHORT).show()
             }
             Send_Info("OFF")
             window.statusBarColor = ContextCompat.getColor(this@MainActivity, R.color.MotorOff)
         }
         else{
             layoutParams.topMargin = 10.toDp(this)
             Switch_Round.setBackgroundResource(R.drawable.switchbutton)
             BlueBack.setBackgroundResource(R.drawable.round_borders)
             connectToWPAWiFi(Config.SSID,Config.PASS)
             status = 0

             timer_to_run = 1;
             current =LocalDateTime.now()
             To_Run = object : Thread() {

                 @RequiresApi(Build.VERSION_CODES.O)
                 override fun run() {
                     try {
                         while(timer_to_run == 1){
                             var last = LocalDateTime.now();

                             var minutes = last.minute - current.minute
                             var hour = last.hour - current.hour
                             var seconds = last.second - current.second

                             var netseconds = (hour * 60) + (minutes * 60) + seconds

                             var required_min = netseconds/60;
                             var required_sec = netseconds%60;


                             var output: String = "$required_min : $required_sec"

                             val timer: TextView = findViewById(R.id.Timer)
                             timer.text = output
                             Thread.sleep(1000)
                         }
                     } catch (Ex: Exception) {
                         //Toast.makeText(MainActivity, "Error $Ex", Toast.LENGTH_SHORT).show()
                     }
                 }
             }
             try {
                 To_Run.start()
             }catch (ex:java.lang.Exception){
                 Toast.makeText(this, "${ex.toString()}", Toast.LENGTH_SHORT).show()
             }

             Send_Info("ON")
             window.statusBarColor = ContextCompat.getColor(this@MainActivity, R.color.AutoMoto)
         }
         Switch_Round.layoutParams = layoutParams

     }




    fun Get_Wifi_Strength(){

        var To_Strength = object:Thread(){
            override fun run() {
                try {
                    while(true){
                        val wifiManager:WifiManager= applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                        val rssi = wifiManager.connectionInfo.rssi
                        val level = WifiManager.calculateSignalLevel(rssi, 10)
                        val per:Int = (level / 10.0 * 100).toInt()


                        val signal : TextView = findViewById(R.id.Signal)

                        signal.text = "$per %"
                        Thread.sleep(30000)
                    }

                } catch (e: java.lang.Exception) {

                }
            }
        }

        To_Strength.start()
    }

    fun Get_Wifi_Name():String
    {
            try{
                val wifiManager = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo: WifiInfo

                wifiInfo = wifiManager.connectionInfo
                if (wifiInfo.supplicantState == SupplicantState.COMPLETED) {
                    val ssid:String = wifiInfo.ssid

                    val pumpname : TextView = findViewById(R.id.Pump_Name)
                    pumpname.text = ssid
                }
            }
            catch (e:java.lang.Exception)
            {
                val pumpname : TextView = findViewById(R.id.Pump_Name)
                pumpname.text = "AutoMoto Motor"
            }
        return ""
    }

    fun Send_Info(str:String)
    {
        try{
            val Output = Communication.Start(str)
            //Toast.makeText(this, "Output :- " + Output, Toast.LENGTH_SHORT).show()
        }
        catch (ex:Exception)
        {
            //Toast.makeText(this, "Error:- ${ex.toString()}", Toast.LENGTH_SHORT).show()
        }
    }


     fun Int.toDp(context: Context):Int = TypedValue.applyDimension(
         TypedValue.COMPLEX_UNIT_DIP,this.toFloat(),context.resources.displayMetrics
     ).toInt()


     //connects to the given ssid
     fun connectToWPAWiFi(ssid:String,pass:String){
         if(isConnectedTo(ssid)){ //see if we are already connected to the given ssid
             //Toast.makeText(this, "Connected to"+ssid, Toast.LENGTH_SHORT).show()
             return
         }
         val wm:WifiManager= applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
         var wifiConfig=getWiFiConfig(ssid)
         if(wifiConfig==null){//if the given ssid is not present in the WiFiConfig, create a config for it
             wifiConfig= createWPAProfile(ssid,pass)
             //Toast.makeText(this, "WiFi Config Created", Toast.LENGTH_SHORT).show()
              //getWiFiConfig(ssid)
         }
         try {
             wm.disconnect()
             val nid: Int = wifiConfig?.networkId ?: 0
             //Toast.makeText(this, "NID $nid", Toast.LENGTH_SHORT).show()
             wm.enableNetwork(nid, true)
             wm.reconnect()
             //Toast.makeText(this, "Connecting to ${wm.connectionInfo} ${wifiConfig?.SSID}", Toast.LENGTH_SHORT).show()
             Log.d(TAG, "intiated connection to SSID" + ssid);
         }
         catch (ex:Exception )
         {
             Toast.makeText(this, "Error: " + ex.toString(), Toast.LENGTH_SHORT).show()
         }
     }
     fun isConnectedTo(ssid: String):Boolean{
         val wm:WifiManager= applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
         if(wm.connectionInfo.ssid == ssid){
             return true
         }
         return false
     }
     fun getWiFiConfig(ssid: String): WifiConfiguration? {

         if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             val wm:WifiManager= applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
             val wifiList=  wm.configuredNetworks
             for (item in wifiList){
                 if(item.SSID != null && item.SSID.equals(ssid)){
                     Toast.makeText(this, "WiFi Configured", Toast.LENGTH_SHORT).show()
                     return item
                 }
             }
         }

         //Toast.makeText(this, "WiFi Not Configured", Toast.LENGTH_SHORT).show()

         return null
     }
     fun createWPAProfile(ssid: String,pass: String): WifiConfiguration?{
         Log.d(TAG,"Saving SSID :"+ssid)
         //Toast.makeText(this, "WPA Creating", Toast.LENGTH_SHORT).show()
         val conf = WifiConfiguration()
         conf.SSID = ssid
         conf.preSharedKey = pass
         val wm:WifiManager= applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
         wm.addNetwork(conf)

         //Toast.makeText(this, "WPA Profile Created", Toast.LENGTH_SHORT).show()
         Log.d(TAG,"saved SSID to WiFiManger")

         return conf
     }

    fun Change_UI(str:String)
    {
        val timer:TextView = findViewById(R.id.Timer)
        timer.text = str
    }



    class WiFiChngBrdRcr : BroadcastReceiver(){ // shows a toast message to the user when device is connected to a AP
         private val TAG = "WiFiChngBrdRcr"
         override fun onReceive(context: Context, intent: Intent) {
             val networkInfo=intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
             if(networkInfo.state == NetworkInfo.State.CONNECTED){
                 val bssid=intent.getStringExtra(WifiManager.EXTRA_BSSID)
                 Log.d(TAG, "Connected to BSSID:"+bssid)
                 val ssid=intent.getParcelableExtra<WifiInfo>(WifiManager.EXTRA_WIFI_INFO).ssid
                 val log="Connected to SSID:"+ssid
                 Log.d(TAG,"Connected to SSID:"+ssid)
                 //Toast.makeText(context, log, Toast.LENGTH_SHORT).show()
             }
         }
     }

}
