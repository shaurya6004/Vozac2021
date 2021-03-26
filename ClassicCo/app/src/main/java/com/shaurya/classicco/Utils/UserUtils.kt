package com.shaurya.classicco.Utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import com.shaurya.classicco.Common
import com.shaurya.classicco.Model.EventBus.NotifyRiderEvent
import com.shaurya.classicco.Model.FCMSendData
import com.shaurya.classicco.Model.TokenModel
import com.shaurya.classicco.R
import com.shaurya.classicco.Remote.IFCMService
import com.shaurya.classicco.Remote.RetrofitFCMClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

object UserUtils {
    fun updateUser(
        view: View?,
        updateData:Map<String,Any>
    ){
        FirebaseDatabase.getInstance()
            .getReference(Common.DRIVER_INFO_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .updateChildren(updateData)
            .addOnFailureListener { e ->
                Snackbar.make(view!!,e.message!!,Snackbar.LENGTH_LONG).show()
            }.addOnSuccessListener {
                Snackbar.make(view!!,"Update information Success",Snackbar.LENGTH_LONG).show()
            }
    }

    fun updateToken(context: Context, token: String) {
        val tokenModel = TokenModel()
        tokenModel.token = token

        FirebaseDatabase.getInstance()
            .getReference(Common.TOKEN_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .setValue(tokenModel)
            .addOnFailureListener { e -> Toast.makeText(context,e.message, Toast.LENGTH_LONG).show() }
            .addOnSuccessListener {  }

    }

    fun sendDeclineRequest(view: View, activity: Activity, key: String) {

        val compositeDisposable = CompositeDisposable()
        val ifcmService = RetrofitFCMClient.instance!!.create(IFCMService::class.java)

        //Get token
        FirebaseDatabase.getInstance()
            .getReference(Common.TOKEN_REFERENCE)
            .child(key)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(databaseError:DatabaseError){
                    Snackbar.make(view,databaseError.message,Snackbar.LENGTH_LONG).show()
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists())
                    {
                        val tokenModel = dataSnapshot.getValue(TokenModel::class.java)
                        val notificationData:MutableMap<String,String> = HashMap()
                        notificationData.put(Common.NOTI_TITLE,Common.REQUEST_DRIVER_DECLINE)
                        notificationData.put(Common.NOTI_BODY, "This message represent for decline action from Driver")
                        notificationData.put(Common.DRIVER_KEY,FirebaseAuth.getInstance().currentUser!!.uid)

                        val fcmSendData = FCMSendData(tokenModel!!.token,notificationData)

                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)!!
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ FCMResponse ->
                                if (FCMResponse!!.success == 0)
                                {
                                    compositeDisposable.clear()
                                    Snackbar.make(view,activity.getString(R.string.decline_failed),Snackbar.LENGTH_LONG).show()
                                }
                                else
                                {
                                    Snackbar.make(view,activity.getString(R.string.decline_success),Snackbar.LENGTH_LONG).show()

                                }

                            },{t : Throwable? ->
                                compositeDisposable.clear()
                                Snackbar.make(view,t!!.message!!, Snackbar.LENGTH_LONG).show()
                            }))
                    }
                    else
                    {
                        compositeDisposable.clear()
                        Snackbar.make(view,activity.getString(R.string.token_not_found),Snackbar.LENGTH_LONG).show()
                    }
                }


            })


    }

    fun sendAcceptRequestToRider(
        view: View?,
        requireContext: Context,
        key: String,
        tripNumberId: String?
    ) {
        val compositeDisposable = CompositeDisposable()
        val ifcmService = RetrofitFCMClient.instance!!.create(IFCMService::class.java)

        //Get token
        FirebaseDatabase.getInstance()
            .getReference(Common.TOKEN_REFERENCE)
            .child(key)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(databaseError:DatabaseError){
                    Snackbar.make(view!!,databaseError.message,Snackbar.LENGTH_LONG).show()
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists())
                    {
                        val tokenModel = dataSnapshot.getValue(TokenModel::class.java)
                        val notificationData:MutableMap<String,String> = HashMap()
                        notificationData.put(Common.NOTI_TITLE,Common.REQUEST_DRIVER_ACCEPT)
                        notificationData.put(Common.NOTI_BODY, "This message represent for accept action from Driver")
                        notificationData.put(Common.DRIVER_KEY,FirebaseAuth.getInstance().currentUser!!.uid)
                        notificationData.put(Common.TRIP_KEY,tripNumberId!!)

                        val fcmSendData = FCMSendData(tokenModel!!.token,notificationData)

                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)!!
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ FCMResponse ->
                                if (FCMResponse!!.success == 0)
                                {
                                    compositeDisposable.clear()
                                    Snackbar.make(view!!,requireContext.getString(R.string.accept_failed),Snackbar.LENGTH_LONG).show()
                                }

                            },{t : Throwable? ->
                                compositeDisposable.clear()
                                Snackbar.make(view!!,t!!.message!!, Snackbar.LENGTH_LONG).show()
                            }))
                    }
                    else
                    {
                        compositeDisposable.clear()
                        Snackbar.make(view!!,requireContext.getString(R.string.token_not_found),Snackbar.LENGTH_LONG).show()
                    }
                }


            })
    }

    fun sendNotifyToRider(context: Context, view: View, key: String?) {
        val compositeDisposable = CompositeDisposable()
        val ifcmService = RetrofitFCMClient.instance!!.create(IFCMService::class.java)

        //Get token
        FirebaseDatabase.getInstance()
            .getReference(Common.TOKEN_REFERENCE)
            .child(key!!)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(databaseError:DatabaseError){
                    Snackbar.make(view!!,databaseError.message,Snackbar.LENGTH_LONG).show()
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists())
                    {
                        val tokenModel = dataSnapshot.getValue(TokenModel::class.java)
                        val notificationData:MutableMap<String,String> = HashMap()
                        notificationData.put(Common.NOTI_TITLE,context.getString(R.string.driver_arrived))
                        notificationData.put(Common.NOTI_BODY, context.getString(R.string.your_driver_arrived))
                        notificationData.put(Common.DRIVER_KEY,FirebaseAuth.getInstance().currentUser!!.uid)
                        notificationData.put(Common.RIDER_KEY,key!!)

                        val fcmSendData = FCMSendData(tokenModel!!.token,notificationData)

                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)!!
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ FCMResponse ->
                                if (FCMResponse!!.success == 0)
                                {
                                    compositeDisposable.clear()
                                    Snackbar.make(view!!,context.getString(R.string.accept_failed),Snackbar.LENGTH_LONG).show()
                                }else
                                    EventBus.getDefault().postSticky(NotifyRiderEvent())

                            },{t : Throwable? ->
                                compositeDisposable.clear()
                                Snackbar.make(view!!,t!!.message!!, Snackbar.LENGTH_LONG).show()
                            }))
                    }
                    else
                    {
                        compositeDisposable.clear()
                        Snackbar.make(view!!,context.getString(R.string.token_not_found),Snackbar.LENGTH_LONG).show()
                    }
                }


            })

    }

    fun sendDeclineAndRemoveTripRequest(view: View, activity: FragmentActivity, key: String, tripNumberId: String?) {

        val compositeDisposable = CompositeDisposable()
        val ifcmService = RetrofitFCMClient.instance!!.create(IFCMService::class.java)

        //First remove trip id

        FirebaseDatabase.getInstance().getReference(Common.TRIP)
            .child(tripNumberId!!)
            .removeValue()
            .addOnFailureListener { e ->
                Snackbar.make(view,e.message!!,Snackbar.LENGTH_SHORT).show()
            }.addOnSuccessListener {
                //After success we will send notification to the rider
                //Get token
                FirebaseDatabase.getInstance()
                    .getReference(Common.TOKEN_REFERENCE)
                    .child(key)
                    .addListenerForSingleValueEvent(object:ValueEventListener{
                        override fun onCancelled(databaseError:DatabaseError){
                            Snackbar.make(view,databaseError.message,Snackbar.LENGTH_LONG).show()
                        }
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists())
                            {
                                val tokenModel = dataSnapshot.getValue(TokenModel::class.java)
                                val notificationData:MutableMap<String,String> = HashMap()
                                notificationData.put(Common.NOTI_TITLE,Common.REQUEST_DRIVER_DECLINE_AND_REMOVE_TRIP)
                                notificationData.put(Common.NOTI_BODY, "This message represent for decline action from Driver")
                                notificationData.put(Common.DRIVER_KEY,FirebaseAuth.getInstance().currentUser!!.uid)

                                val fcmSendData = FCMSendData(tokenModel!!.token,notificationData)

                                compositeDisposable.add(ifcmService.sendNotification(fcmSendData)!!
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ FCMResponse ->
                                        if (FCMResponse!!.success == 0)
                                        {
                                            compositeDisposable.clear()
                                            Snackbar.make(view,activity.getString(R.string.decline_failed),Snackbar.LENGTH_LONG).show()
                                        }
                                        else
                                        {
                                            Snackbar.make(view,activity.getString(R.string.decline_success),Snackbar.LENGTH_LONG).show()

                                        }

                                    },{t : Throwable? ->
                                        compositeDisposable.clear()
                                        Snackbar.make(view,t!!.message!!, Snackbar.LENGTH_LONG).show()
                                    }))
                            }
                            else
                            {
                                compositeDisposable.clear()
                                Snackbar.make(view,activity.getString(R.string.token_not_found),Snackbar.LENGTH_LONG).show()
                            }
                        }


                    })

            }

    }

    fun sendCompleteTripToRider(view: View, context: Context, key: String?, tripNumberId: String) {
        val compositeDisposable = CompositeDisposable()
        val ifcmService = RetrofitFCMClient.instance!!.create(IFCMService::class.java)

        //First remove trip id


                //After success we will send notification to the rider
                //Get token
                FirebaseDatabase.getInstance()
                    .getReference(Common.TOKEN_REFERENCE)
                    .child(key!!)
                    .addListenerForSingleValueEvent(object:ValueEventListener{
                        override fun onCancelled(databaseError:DatabaseError){
                            Snackbar.make(view,databaseError.message,Snackbar.LENGTH_LONG).show()
                        }
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists())
                            {
                                val tokenModel = dataSnapshot.getValue(TokenModel::class.java)
                                val notificationData:MutableMap<String,String> = HashMap()
                                notificationData.put(Common.NOTI_TITLE,Common.RIDER_REQUEST_COMPLETE_TRIP)
                                notificationData.put(Common.NOTI_BODY, "This message represent for request complete to Rider")
                                notificationData.put(Common.TRIP_KEY,tripNumberId!!)

                                val fcmSendData = FCMSendData(tokenModel!!.token,notificationData)

                                compositeDisposable.add(ifcmService.sendNotification(fcmSendData)!!
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ FCMResponse ->
                                        if (FCMResponse!!.success == 0)
                                        {
                                            compositeDisposable.clear()
                                            Snackbar.make(view,context.getString(R.string.complete_trip_failed),Snackbar.LENGTH_LONG).show()
                                        }
                                        else
                                        {
                                            Snackbar.make(view,context.getString(R.string.complete_trip_success),Snackbar.LENGTH_LONG).show()

                                        }

                                    },{t : Throwable? ->
                                        compositeDisposable.clear()
                                        Snackbar.make(view,t!!.message!!, Snackbar.LENGTH_LONG).show()
                                    }))
                            }
                            else
                            {
                                compositeDisposable.clear()
                                Snackbar.make(view,context.getString(R.string.token_not_found),Snackbar.LENGTH_LONG).show()
                            }
                        }


                    })

            }


}