package com.ztiany.androidx

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ztiany.androidx.jetpack.compatibility.CompatibilityActivity
import com.ztiany.androidx.jetpack.datastore.DataStoreActivity
import com.ztiany.androidx.jetpack.lifecycle.LifecycleActivity
import com.ztiany.androidx.jetpack.livedata.LiveDataActivity
import com.ztiany.androidx.jetpack.viewmodel.ViewModelActivity
import com.ztiany.androidx.kotlin.R
import com.ztiany.androidx.kotlin.coroutines.official.google.flow.OfficialFlowActivity
import com.ztiany.androidx.kotlin.coroutines.research.CoroutineWithGlobalScopeActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun openJetpackAppCompat(view: View) {
        startActivity(Intent(this, CompatibilityActivity::class.java))
    }

    fun viewModelWithGlobalScope(view: View) {
        startActivity(Intent(this, CoroutineWithGlobalScopeActivity::class.java))
    }

    fun openJetpackLifecycle(view: View) {
        startActivity(Intent(this, LifecycleActivity::class.java))
    }

    fun openJetpackLiveData(view: View) {
        startActivity(Intent(this, LiveDataActivity::class.java))
    }

    fun openJetpackViewModel(view: View) {
        startActivity(Intent(this, ViewModelActivity::class.java))
    }

    fun openJetpackDataStore(view: View) {
        startActivity(Intent(this, DataStoreActivity::class.java))
    }

    fun openKotlinGoogleFlow(view: android.view.View) {
        startActivity(Intent(this, OfficialFlowActivity::class.java))
    }

}
