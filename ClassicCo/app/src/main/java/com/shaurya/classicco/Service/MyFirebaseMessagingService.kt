package com.shaurya.classicco.Service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shaurya.classicco.Common
import com.shaurya.classicco.Model.EventBus.DriverRequestReceived
import com.shaurya.classicco.Utils.UserUtils
import org.greenrobot.eventbus.EventBus
import kotlin.random.Random

class MyFirebaseMessagingService :FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if(FirebaseAuth.getInstance().currentUser != null)
            UserUtils.updateToken(this,token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data =remoteMessage.data
        if (data != null)
        {
            if (data[Common.NOTI_TITLE].equals(Common.REQUEST_DRIVER_TITLE)){


                val driverRequestReceived = DriverRequestReceived()
                driverRequestReceived.key = data[Common.RIDER_KEY]
                driverRequestReceived.pickupLocation = data[Common.PICKUP_LOCATION]
                driverRequestReceived.pickupLocationString = data[Common.PICKUP_LOCATION_STRING]
                driverRequestReceived.destinationLocation = data[Common.DESTINATION_LOCATION]
                driverRequestReceived.destinationLocationString = data[Common.DESTINATION_LOCATION_STRING]


                EventBus.getDefault().postSticky(DriverRequestReceived())

            }else
            Common.showNotification(this, Random.nextInt(),
            data[Common.NOTI_TITLE],
            data[Common.NOTI_BODY],
            null)
        }
    }
}