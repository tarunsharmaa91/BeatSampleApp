package com.app.beatsampleapp.viewmodels

import android.location.Location
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import com.app.beatsampleapp.model.LocationResult
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

class MainActivityViewModelTest {
    lateinit var viewModel: MainActivityViewModel

    @Before
    fun setup() {
        viewModel = MainActivityViewModel()
        viewModel.locationResultsError = MutableLiveData()
        viewModel.locationResults = MutableLiveData()
    }

    @Test
    @Throws(Exception::class)
    fun test_Category() {
        viewModel.locationResults = mock()
        val observer = lambdaMock<(ArrayList<LocationResult>) -> Unit>()
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        viewModel.locationResults.observe({ lifecycle }) {
            it?.let(observer)
        }

        viewModel.refresh(mock(Location::class.java))

        Mockito.verify(viewModel.locationResults, Mockito.times(1)).postValue(Mockito.any())
    }

    private inline fun <reified T> lambdaMock(): T = mock(T::class.java)
    private inline fun <reified T : Any> mock() = mock(T::class.java)
}