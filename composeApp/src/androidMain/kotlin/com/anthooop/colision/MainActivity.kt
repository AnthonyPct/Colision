package com.anthooop.colision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.anthooop.colision.app.App
import com.anthooop.colision.core.di.androidPlatformModule
import com.anthooop.colision.core.di.appModule
import com.anthooop.colision.core.di.coreModule
import com.anthooop.colision.core.di.featureModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@MainActivity.applicationContext)
                modules(appModule, coreModule, androidPlatformModule, *featureModules.toTypedArray())
            }
        }

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
