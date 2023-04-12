package com.devlomi.fireapp.activities.placespicker

import android.annotation.SuppressLint
import android.arch.lifecycle.*
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.LocationManager
import android.util.Log
import com.devlomi.fireapp.common.extensions.toLatLng
import com.devlomi.fireapp.common.extensions.toLatLngString
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext


class PlacesPickerViewModel(context: Context, lifecycleOwner: LifecycleOwner) : ViewModel(), LifecycleObserver, CoroutineScope {

    private val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
    private var locationRequest: LocationRequest? = null
    private val mFusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            Log.d("3llomi", "posting last location")
            p0?.lastLocation?.let {
                currentLocationLiveData.value = it.toLatLng()
            }
        }

        override fun onLocationAvailability(p0: LocationAvailability?) {
            super.onLocationAvailability(p0)
            Log.d("3llomi","onLocationAvailabillty $p0" )
        }
    }
    val showLocationDialogLiveData = MutableLiveData<Unit>()
    val currentLocationLiveData = MutableLiveData<LatLng>()
    val nearbyPlacesLiveData = MutableLiveData<List<Place>>()

    private val onLocationChangedBehavior = BehaviorSubject.create<LatLng>()
    private var placesWebService = PlacesWebService()
    private lateinit var job: Job


    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    init {
        lifecycleOwner.lifecycle.addObserver(this)
        onLocationChangedBehavior
                .debounce(2, TimeUnit.SECONDS)
                .distinctUntilChanged()
                .subscribe { getCurrentPlace(it) }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        job = Job()

        initLocationRequest()
    }


    fun markerMoved(latLng: LatLng) {
        if (isLocationEnabled())
            onLocationChangedBehavior.onNext(latLng)
        else
            requestLocationDialog()
    }


    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        Log.d("3llomi", "getCurrent Location")
        if (!isLocationEnabled()) {
            requestLocationDialog()
        } else {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun requestLocationDialog() {
        showLocationDialogLiveData.value = Unit
    }

    private fun isLocationEnabled() = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)


    @SuppressLint("MissingPermission")
    private fun getCurrentPlace(latLng: LatLng) {

        launch(job) {
            try {
                val response = placesWebService.getNearbyPlaces(latLng.toLatLngString()).await()
                val places = response.response.venues.map {
                    Place(
                            it.name,
                            if (it.location.formattedAddress.isEmpty()) "" else it.location.formattedAddress[0],
                            if (it.categories.isEmpty()) null else it.categories[0].icon.getIcon(44),
                            LatLng(it.location.lat, it.location.lng)
                    )
                }.toList()

                Log.d("3llomi", "response is $response")

                nearbyPlacesLiveData.value = places

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        job.cancel()
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() {
    }


    private fun initLocationRequest() {
        if (locationRequest == null) {
            locationRequest = LocationRequest.create()
            locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest!!.numUpdates = 1
        }
    }

}