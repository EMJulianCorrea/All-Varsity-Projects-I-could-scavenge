package com.drivemate.sa.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.drivemate.sa.network.CreateCloudTestRequest
import com.drivemate.sa.network.RetrofitClient
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    currentUser: FirebaseUser?,
    isLoading: Boolean,
    errorMessage: String?,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onOpenMaintenance: () -> Unit
) {
    var apiStatus by remember { mutableStateOf("Checking API...") }
    var cloudStatus by remember { mutableStateOf("Cloud database not tested yet") }
    var cloudLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        apiStatus = try {
            val response = RetrofitClient.api.getHealth()
            "${response.service}: ${response.status}"
        } catch (e: Exception) {
            Log.e("DriveMateApi", "API connection failed", e)
            "API connection failed"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DriveMate",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your vehicle companion",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = apiStatus,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !cloudLoading,
            onClick = {
                scope.launch {
                    cloudLoading = true

                    cloudStatus = try {
                        RetrofitClient.api.createCloudTestRecord(
                            CreateCloudTestRequest(
                                message = "Hello from DriveMate Android"
                            )
                        )

                        val records = RetrofitClient.api.getCloudTestRecords()
                        val latestRecord = records.firstOrNull()

                        if (latestRecord != null) {
                            "Cloud DB connected: ${latestRecord.message}"
                        } else {
                            "Record created, but no records returned."
                        }
                    } catch (e: Exception) {
                        Log.e("DriveMateCloud", "Cloud database test failed", e)
                        "Cloud database test failed"
                    } finally {
                        cloudLoading = false
                    }
                }
            }
        ) {
            Text(
                if (cloudLoading) "Testing cloud database..." else "Test Cloud Database"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = cloudStatus,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (currentUser == null) {
            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                onClick = onSignIn
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("Sign in with Google")
                }
            }

            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Text(
                text = "Welcome",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentUser.displayName ?: currentUser.email ?: "DriveMate User"
            )

            currentUser.email?.let { email ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(email)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenMaintenance
            ) {
                Text("Vehicle Health")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSignOut
            ) {
                Text("Sign out")
            }
        }
    }
}
