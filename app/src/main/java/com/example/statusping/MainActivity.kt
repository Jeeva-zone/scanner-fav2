package com.example.statusping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.statusping.ui.StatusScreen
import com.example.statusping.ui.theme.StatusPingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StatusPingTheme {
                val viewModel: StatusViewModel = viewModel()
                StatusScreen(viewModel = viewModel)
            }
        }
    }
}
