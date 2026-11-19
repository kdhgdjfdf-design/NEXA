package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.domain.i18n.LocalNexaStrings
import com.example.domain.security.CryptoAndLockManager

@Composable
fun SectionLockGate(
    sectionTitle: String,
    pinHash: String,
    pinSalt: String,
    biometricEnabled: Boolean,
    onUnlocked: () -> Unit
) {
    val strings = LocalNexaStrings.current
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val canBiometric = biometricEnabled && CryptoAndLockManager.isBiometricAvailable(context) && context is FragmentActivity

    LaunchedEffect(canBiometric) {
        if (canBiometric) {
            CryptoAndLockManager.promptBiometric(
                activity = context as FragmentActivity,
                title = "NEXA • $sectionTitle",
                subtitle = strings.biometricLock,
                negativeText = strings.setPin,
                onSuccess = onUnlocked,
                onError = { /* Fallback to PIN */ }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        NexaCard(
            modifier = Modifier.widthIn(max = 420.dp),
            cornerRadius = 24.dp,
            elevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
                Text(
                    text = sectionTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = {
                        enteredPin = it.filter { ch -> ch.isDigit() }.take(8)
                        errorMsg = null
                    },
                    label = { Text(strings.setPin) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = {
                        if (CryptoAndLockManager.verifyPin(enteredPin, pinHash, pinSalt)) {
                            onUnlocked()
                        } else {
                            errorMsg = strings.invalidPinError
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(strings.confirm)
                }
                if (canBiometric) {
                    OutlinedButton(
                        onClick = {
                            CryptoAndLockManager.promptBiometric(
                                activity = context as FragmentActivity,
                                title = "NEXA • $sectionTitle",
                                subtitle = strings.biometricLock,
                                negativeText = strings.cancel,
                                onSuccess = onUnlocked,
                                onError = { msg -> errorMsg = msg }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Fingerprint, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.biometricLock)
                    }
                }
            }
        }
    }
}
