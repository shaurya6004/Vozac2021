package com.shaurya.classicco

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng
import com.shaurya.classicco.Model.DriverInfoModel
import java.lang.StringBuilder
import kotlin.random.Random

object Common {
    fun buildWelcomeMessage(): String {
        return StringBuilder("Welcome, ")
            .append(currentUser!!.firstName)
            .append("")
            .append(currentUser!!.lastName)
            .toString()

    }

    fun showNotification(context: Context, id: Int, title: String?, body: String?, intent: Intent?) {
        var pendingIntent: PendingIntent? = null
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context,id,intent!!,PendingIntent.FLAG_UPDATE_CURRENT)
        val NOTIFICATION_CHANNEL_ID = "com_shaurya_classico"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,"DriveCo",
                NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.description = "Drive Co"
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.RED
                notificationChannel.vibrationPattern = longArrayOf(0,1000,500,1000)
                notificationChannel.enableVibration(true)

                notificationManager.createNotificationChannel(notificationChannel)

        }
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setSmallIcon(R.drawable.ic_baseline_directions_car_24)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources,
                R.drawable.ic_baseline_directions_car_24
            ))
        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent!!)
        val notification = builder.build()
        notificationManager.notify(id,notification)



    }

    fun decodePoly(encoded:String): ArrayList<LatLng>{
        val poly = ArrayList<LatLng>()
        var index = 0
        var len = encoded.length
        var lat = 0
        var lng = 0
        while (index<len){
            var b:Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            }while (b >= 0x20)
            val dlat = if (result and 1 !=0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            }while (b >=0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p= LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)

        }

        return poly
    }

    fun createUniqueTripIdNumber(timeOffset: Long?): String? {
        val rd = Random
        var current = System.currentTimeMillis()+timeOffset!!
        var unique = current+rd.nextLong()
        if (unique<0) unique *= -1
        return unique.toString()

    }


    val RIDER_REQUEST_COMPLETE_TRIP: String="RequestCompletetripToRider"
    val REQUEST_DRIVER_DECLINE_AND_REMOVE_TRIP: String="DeclineAndRemoveTrip"
    val TRIP_DESTINSTION_LOCATION_REF: String = "TripDestinationLocation"
    val WAIT_TIME_IN_MIN: Int = 1
    val MIN_RANGE_PICKUP_IN_KM: Double = 0.05 // 50 m
    val TRIP_PICKUP_REF: String = "TripPickupLocation"
    val TRIP_KEY: String = "Key"
    val REQUEST_DRIVER_ACCEPT: String = "Accept"
    val TRIP: String = "Trip"
    val RIDER_INFO: String = "Riders"
    val DRIVER_KEY: String= "Driver Key"
    val REQUEST_DRIVER_DECLINE: String="Driver request decline"
    val NOTI_BODY: String = "body"
    val NOTI_TITLE: String = "title"

    val RIDER_KEY: String = "RiderKey"
    val PICKUP_LOCATION: String = "Pickup Location"
    val REQUEST_DRIVER_TITLE: String="Request Driver"

    val DRIVER_LOCATION_REFERENCE: String="DriversLocation"
    val DRIVER_INFO_REFERENCE : String = "DriverInfo"
    var currentUser: DriverInfoModel?= null
    val TOKEN_REFERENCE: String = "Token"

    val DESTINATION_LOCATION_STRING: String = "DestinationLocationString"
    val PICKUP_LOCATION_STRING: String = "PickupLocationString"
    val DESTINATION_LOCATION:String = "DestinationLocation"

}