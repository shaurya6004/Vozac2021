package com.shaurya.classicco.Model

class FCMResponse {
    var multicast_id:Long=0
    var success = 0
    var failure = 0
    var canonical_ids = 0
    var result:List<FCMResult>? = null
    var message_id:Long = 0
}