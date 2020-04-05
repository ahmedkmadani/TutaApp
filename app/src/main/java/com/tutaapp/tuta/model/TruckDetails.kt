package com.tutaapp.tuta.model

class TrucksDetails {
    var id: Int? = null
    var vehicle_id: Int? = null
    var latitude: String? = null
    var longitude: String? = null
    var created_at: String? = null
    var updated_at: String? = null
    var deleted_at: String? = null
    var vehicle: String? = null


    constructor(id: Int, vehicle_id: Int, latitude: String, longitude: String, created_at: String, updated_at: String, deleted_at: String, vehicle: String) {
        this.id = id
        this.vehicle_id = vehicle_id
        this.latitude = latitude
        this.created_at = created_at
        this.updated_at = updated_at
        this.deleted_at = deleted_at
        this.longitude = longitude
        this.vehicle = vehicle


    }
}