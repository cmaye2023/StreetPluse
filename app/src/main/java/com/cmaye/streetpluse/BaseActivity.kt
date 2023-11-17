package com.cmaye.streetpluse

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cmaye.streetpluse.databinding.ActivityBaseBinding


class BaseActivity : AppCompatActivity(){
    private lateinit var binding: ActivityBaseBinding

    private var permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){isGranted ->
        if (isGranted)
        {
            Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermission()
        onClickListener()
    }
    private fun requestPermission()
    {
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun onClickListener()
    {
        binding.btnOverlay.setOnClickListener {
            val intent = Intent(this,LocationOverlayActivity::class.java)
            startActivity(intent)
        }
        binding.btnLocationManager.setOnClickListener {
            val intent = Intent(this,LocationManagerActivity::class.java)
            startActivity(intent)
        }
        binding.btnFusedLocation.setOnClickListener {
            val intent = Intent(this,FusedLocationActivity::class.java)
            startActivity(intent)
        }


    }
}