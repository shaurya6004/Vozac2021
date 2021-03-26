package com.shaurya.classicco.Model

class TripPlanModel {
    var rider:String?=null
    var driver:String?=null
    var driverInfoModel:DriverInfoModel?=null
    var riderModel:RiderModel?=null
    var origin:String?=null
    var originString:String?=null
    var destination:String?=null
    var destinationString:String?=null
    var distancePickup:String?=null
    var durationPickup:String?=null
    var distanceDestination:String?=null
    var durationDestination:String?=null
    var currentLat:Double= -1.0
    var currentLng:Double= -1.0
    var isDone = false
    var isCancel = false
}