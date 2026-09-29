package com.bellezaintegral.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bellezaintegral.app.data.ApiFactory
import com.bellezaintegral.app.data.BellezaRepository
import com.bellezaintegral.app.data.SessionStore
import com.bellezaintegral.app.ui.BellezaApp
import com.bellezaintegral.app.ui.BellezaViewModel
import com.bellezaintegral.app.ui.BellezaViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionStore(applicationContext)
        val repository = BellezaRepository(
            api = ApiFactory.create(session),
            session = session
        )
        val factory = BellezaViewModelFactory(repository)

        setContent {
            val vm: BellezaViewModel = viewModel(factory = factory)
            BellezaApp(vm)
        }
    }
}
