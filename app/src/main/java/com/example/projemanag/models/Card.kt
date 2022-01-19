package com.example.projemanag.models

import android.os.Parcel
import android.os.Parcelable

data class Card (
    val name:String = "",
    val createdBy:String="",
    val assignedTo:ArrayList<String> = ArrayList()
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel, p1: Int) = with(p0) {
        writeString(name)
        writeString(createdBy)
        writeStringList(assignedTo)
    }

    companion object CREATOR : Parcelable.Creator<Card> {
        override fun createFromParcel(parcel: Parcel): Card {
            return Card(parcel)
        }

        override fun newArray(size: Int): Array<Card?> {
            return arrayOfNulls(size)
        }
    }
}