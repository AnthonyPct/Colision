package com.anthooop.colision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.anthooop.colision.app.App
import com.anthooop.colision.core.common.ProjectSyncManager
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val syncManager: ProjectSyncManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        // Pull-on-foreground (FR40): every time the user returns to the app
        // (cold start, after backgrounding) we refresh the local Room cache
        // from Supabase. Connectivity-based refresh is owned by the manager.
        syncManager.refreshNow()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
