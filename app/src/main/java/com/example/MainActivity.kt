package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CallOverlayScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.NovaChatViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: NovaChatViewModel = viewModel()
      val isDarkTheme by viewModel.isDarkTheme.collectAsState()

      MyApplicationTheme(darkTheme = isDarkTheme, dynamicColor = false) {
        val authState by viewModel.authUiState.collectAsState()
        val selectedChat by viewModel.selectedChat.collectAsState()
        val callState by viewModel.callState.collectAsState()

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            // Main navigation router based on Auth and Navigation states
            when (val state = authState) {
              is AuthUiState.Unauthenticated, is AuthUiState.Registering, is AuthUiState.Error -> {
                AuthScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
              }
              is AuthUiState.Loading -> {
                LoadingDerivingScreen()
              }
              is AuthUiState.Authenticated -> {
                if (selectedChat == null) {
                  MainScreen(viewModel = viewModel, currentUser = state.user, modifier = Modifier.fillMaxSize())
                } else {
                  ChatDetailScreen(viewModel = viewModel, currentUser = state.user)
                }
              }
            }

            // WebRTC Call overlay layer (renders on top of any active screen)
            if (callState.callStage != com.example.ui.viewmodel.CallStage.IDLE) {
              CallOverlayScreen(viewModel = viewModel, call = callState)
            }
          }
        }
      }
    }
  }
}

@Composable
fun LoadingDerivingScreen() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F1013)),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      CircularProgressIndicator(color = Color(0xFFFF2A7A))
      Spacer(modifier = Modifier.height(18.dp))
      Text(
        text = "Deriving Cryptographic Pair Keys...",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
      )
      Text(
        text = "Initializing local Argon2 hashes",
        color = Color.Gray,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 4.dp)
      )
    }
  }
}

