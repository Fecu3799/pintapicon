package com.example.pintapiconv3.app.user.main

import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.pintapiconv3.R
import com.example.pintapiconv3.app.admin.NewPredioActivity.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class ShowMapDialog(private val latitude: Double, private val longitude: Double) : DialogFragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var locationPermissionGranted = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), theme)
        dialog.setContentView(R.layout.dialog_show_on_map)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val mapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commitNow()

        mapFragment.getMapAsync(this)

        return dialog
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        setupMap()

        val location = LatLng(latitude, longitude)
        googleMap.addMarker(MarkerOptions().position(location).title("Ubicación del Predio"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    private fun setupMap() {
        if(ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            googleMap.isMyLocationEnabled = true
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isZoomGesturesEnabled = true
        googleMap.uiSettings.isScrollGesturesEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            locationPermissionGranted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if(locationPermissionGranted) {
                if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                    googleMap.isMyLocationEnabled = true
                }
            } else {
                Toast.makeText(requireContext(), "Es necesario el permiso de ubicacion para seleccionar la ubicacion", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }
}