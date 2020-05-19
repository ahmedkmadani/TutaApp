package com.tutaapp.tuta

import org.json.JSONObject


interface ServerCallback {
    fun onSuccess(result: JSONObject?)
}