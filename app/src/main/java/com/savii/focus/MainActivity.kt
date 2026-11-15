package com.savii.focus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.savii.focus.data.FocusRepository
import com.savii.focus.data.local.FocusDatabase
import com.savii.focus.ui.FocusViewModel
import com.savii.focus.ui.SaviiFocusApp
import com.savii.focus.ui.theme.SaviiFocusTheme

class MainActivity : ComponentActivity() {
    private lateinit var focusViewModel: FocusViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.rgb(13, 11, 20)
        window.navigationBarColor = android.graphics.Color.rgb(13, 11, 20)

        val database = FocusDatabase.getInstance(applicationContext)
        val repository = FocusRepository(database.focusDao())
        val preferences = getSharedPreferences("savii_focus_timer", MODE_PRIVATE)
        focusViewModel = ViewModelProvider(
            this,
            FocusViewModel.Factory(repository, preferences)
        )[FocusViewModel::class.java]

        setContent {
            SaviiFocusTheme {
                SaviiFocusApp(viewModel = focusViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::focusViewModel.isInitialized) {
            focusViewModel.refreshClock()
        }
    }
}
