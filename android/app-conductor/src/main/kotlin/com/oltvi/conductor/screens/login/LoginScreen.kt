package com.oltvi.conductor.screens.login

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.ui.components.OltviButton
import com.oltvi.core.ui.effects.GlassCard
import com.oltvi.core.ui.effects.ParticleField

private enum class LoginStep { PHONE, OTP }

@Composable
fun ConductorLoginScreen(onLoginSuccess: () -> Unit) {
    var step by remember { mutableStateOf(LoginStep.PHONE) }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F1820))) {
        ParticleField(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.DirectionsCar,
                null,
                tint = OltviColors.action,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text("OLTVI Conductor", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Text("Centro de comando", color = OltviColors.action, fontSize = 13.sp, fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(48.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedContent(targetState = step, label = "step") { s ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            when (s) {
                                LoginStep.PHONE -> {
                                    Text("Ingresá tu número", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Verificamos tu identidad como conductor activo",
                                        color = Color.White.copy(0.6f),
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = { if (it.length <= 15) phone = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("+54 9 11 0000-0000", color = Color.White.copy(0.4f)) },
                                        leadingIcon = { Icon(Icons.Filled.Phone, null, tint = OltviColors.action) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = OltviColors.action,
                                            unfocusedBorderColor = Color.White.copy(0.2f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            cursorColor = OltviColors.action
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )
                                    OltviButton(
                                        text = "Continuar",
                                        onClick = { if (phone.length >= 8) step = LoginStep.OTP },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                LoginStep.OTP -> {
                                    Text("Código de verificación", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Ingresá el código enviado a $phone",
                                        color = Color.White.copy(0.6f),
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    OtpRow(
                                        value = otp,
                                        onValueChange = {
                                            if (it.length <= 6) {
                                                otp = it
                                                if (it.length == 6) onLoginSuccess()
                                            }
                                        }
                                    )
                                    OltviButton(
                                        text = "Ingresar al turno",
                                        onClick = { if (otp.length == 6) onLoginSuccess() },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    TextButton(onClick = { step = LoginStep.PHONE; otp = "" }) {
                                        Text("Cambiar número", color = OltviColors.action, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OtpRow(value: String, onValueChange: (String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(6) { index ->
            val char = value.getOrNull(index)
            val isFocused = index == value.length
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .border(2.dp, if (isFocused) OltviColors.action else Color.White.copy(0.2f), RoundedCornerShape(8.dp))
                    .background(Color.White.copy(0.05f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(char?.toString() ?: "", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.size(1.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent
        )
    )
}
