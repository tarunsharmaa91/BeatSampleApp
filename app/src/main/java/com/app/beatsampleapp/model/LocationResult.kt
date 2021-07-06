package com.app.beatsampleapp.model

import Venue
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull

/**
 * UI Model for holding and displaying venue information in the view holders
 */
class LocationResult(private val venue: Venue) : Parcelable {
    val id = venue.id
    val locationName = venue.name
    val locationCategory = venue.categories?.firstOrNull()?.pluralName
    val locationIcon = buildIconPath(venue)
    val lat = venue.location?.lat
    val lng = venue.location?.lng

    constructor(parcel: Parcel) : this(
        parcel.readParcelable<Venue>(
            Venue::class.java.classLoader
        )!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(venue, 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocationResult> {
        override fun createFromParcel(parcel: Parcel): LocationResult {
            return LocationResult(parcel)
        }

        override fun newArray(size: Int): Array<LocationResult?> {
            return arrayOfNulls(size)
        }
    }

    @NonNull
    private fun buildIconPath(venue: Venue): String {
        venue.categories?.firstOrNull()?.icon?.let {
            if (!it.prefix.isNullOrBlank() && !it.suffix.isNullOrBlank()) {
                return it.prefix + "88" + it.suffix
            }
        }

        return ""
    }

   /* override fun areItemsSame(other: ILocationResult): Boolean {
        return other is LocationResult
    }

    override fun areContentsSame(other: ILocationResult): Boolean {
        val otherResult = other as LocationResult
        return id == otherResult.id &&
                locationCategory == otherResult.locationCategory &&
                locationIcon == otherResult.locationIcon &&
                locationName == otherResult.locationName &&
                lat == otherResult.lat &&
                lng == otherResult.lng
    }*/
}