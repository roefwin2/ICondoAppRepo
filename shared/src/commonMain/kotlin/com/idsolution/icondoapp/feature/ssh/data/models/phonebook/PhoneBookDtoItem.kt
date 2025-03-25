package com.idsolution.icondoapp.feature.ssh.data.models.phonebook


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhoneBookDtoItem(
    @SerialName("active")
    val active: Boolean,
    @SerialName("appno")
    val appno: String,
    @SerialName("dialno")
    val dialno: String,
    @SerialName("firstname")
    val firstname: String,
    @SerialName("floor")
    val floor: String,
    @SerialName("ipaddr")
    val ipaddr: String,
    @SerialName("lastname")
    val lastname: String,
    @SerialName("phone")
    val phone: String,
    @SerialName("realid")
    val realid: Int,
    @SerialName("towerno")
    val towerno: String
)