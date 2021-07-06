package com.app.beatsampleapp.viewmodels

import FoursquareResponse
import FoursquareServiceProvider
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.beatsampleapp.model.LocationResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

enum class ApiStatus {
    LOADING,
    ERROR,
    COMPLETED
}

enum class Mode {
    Exploration,
    Navigation
}

class MainActivityViewModel : ViewModel() {
    companion object {
        const val ERROR_CODE_RETRIEVE = 500
        const val ERROR_CODE_NO_CURRENT_LOCATION = 501
    }

    var isNavigationMode: MutableLiveData<Boolean> = MutableLiveData()

    //This is to store the current request status (Internal)
    private val _status = MutableLiveData<ApiStatus>()

    //External Mutable Live Data to store status
    val status: LiveData<ApiStatus>
        get() = _status

    //This is to store the current request status (Internal)
    private val _mode = MutableLiveData<Mode>()

    //External Mutable Live Data to store status
    val mode: LiveData<Mode>
        get() = _mode

    //Below is category code for Food venues
    private var searchFilter: String = "4d4b7105d754a06374d81259"
    var locationResults: MutableLiveData<ArrayList<LocationResult>> = MutableLiveData()
    var selectedResult: MutableLiveData<LocationResult> = MutableLiveData()
    var locationResultsError: MutableLiveData<Int> = MutableLiveData()

    fun changeModeToExploration() {
        isNavigationMode.value = false
        _mode.value = Mode.Exploration
    }

    private fun getLocationResults(query: String, currentLocation: Location) {
        _status.value = ApiStatus.LOADING
        val currentLatLng =
            currentLocation.latitude.toString() + "," + currentLocation.longitude.toString()
        FoursquareServiceProvider.service.getLocationResults(query = query, latlng = currentLatLng)
            .enqueue(object : Callback<FoursquareResponse> {
                override fun onFailure(call: Call<FoursquareResponse>, t: Throwable) {
                    _status.value = ApiStatus.ERROR
                    locationResultsError.postValue(ERROR_CODE_RETRIEVE)
                }

                override fun onResponse(
                    call: Call<FoursquareResponse>,
                    response: Response<FoursquareResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { foursquareResponse ->
                            locationResults.postValue(buildResults(foursquareResponse))
                            _status.value = ApiStatus.COMPLETED
                        }
                    } else {
                        _status.value = ApiStatus.ERROR
                        onFailure(
                            call,
                            Throwable("Unsuccessful request for locations: " + response.code())
                        )
                    }
                }
            })
    }

    fun refresh(currentLocation: Location?) {
        if (currentLocation == null) {
            locationResultsError.postValue(ERROR_CODE_NO_CURRENT_LOCATION)
            return
        }
        getLocationResults(searchFilter, currentLocation)
    }

    /**
     * Build the list of location results
     *
     * @return empty list of no results were found in the response
     */
    private fun buildResults(foursquareResponse: FoursquareResponse): ArrayList<LocationResult> {
        foursquareResponse.response?.let { response ->
            val resultsList = ArrayList<LocationResult>()
            response.venues.forEach {
                resultsList.add(LocationResult(it))
            }
            return resultsList
        }
        return ArrayList()
    }

    fun setNavigationModeItem(selectedLocation: LocationResult) {
        _mode.value = Mode.Navigation
        isNavigationMode.value = true
        selectedResult.postValue(selectedLocation)
    }
}