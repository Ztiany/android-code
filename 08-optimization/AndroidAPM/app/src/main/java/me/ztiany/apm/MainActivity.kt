package me.ztiany.apm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import me.ztiany.apm.databinding.ActivityMainBinding
import me.ztiany.apm.scene.memory.MemorySceneActivity
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpOperationView()
        setUpNavigationView()
    }

    private fun setUpOperationView() = with(binding) {
        btnStartMemoryTracker.setOnClickListener {
            Timber.d("start memory tracker")
            App.jniBridge.hookMemoryAllocation()
        }

        btnDumpMemory.setOnClickListener {
            Timber.d("dump memory info")
            App.jniBridge.dumpMemoryAllocationInfo()
        }
    }


    private fun setUpNavigationView() = with(binding) {
        btnEnterMemoryTrackerScene.setOnClickListener {
            MemorySceneActivity.start(this@MainActivity)
        }
    }


}