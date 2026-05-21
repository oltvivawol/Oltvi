package com.oltvi.jefe.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.ui.components.OltviButton
import com.oltvi.core.ui.effects.GlassCard
import com.oltvi.core.ui.effects.ParticleField

@Composable
fun JefeLoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

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
                Icons.Filled.AdminPanelSettings,
                null,
                tint = OltviColors.action,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text("OLTVI Mando", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Text("Panel de inteligencia operacional", color = OltviColors.action, fontSize = 13.sp, fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(48.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Acceso administrativo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Solo personal autorizado de OLTVI",
                        color = Color.White.copy(0.6f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMsg = "" },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("correo@oltvi.com", color = Color.White.copy(0.4f)) },
                        leadingIcon = { Icon(Icons.Filled.Email, null, tint = OltviColors.action) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OltviColors.action,
                            unfocusedBorderColor = Color.White.copy(0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = OltviColors.action
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        label = { Text("Email", color = Color.White.copy(0.6f)) }
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMsg = "" },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("••••••••", color = Color.White.copy(0.4f)) },
                        leadingIcon = { Icon(Icons.Filled.Lock, null, tint = OltviColors.action) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    null,
                                    tint = Color.White.copy(0.5f)
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OltviColors.action,
                            unfocusedBorderColor = Color.White.copy(0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = OltviColors.action
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        label = { Text("Contraseña", color = Color.White.copy(0.6f)) }
                    )

                    if (errorMsg.isNotEmpty()) {
                        Text(errorMsg, color = OltviColors.error, fontSize = 13.sp)
                    }

                    OltviButton(
                        text = "Ingresar al mando",
                        onClick = {
                            when {
                                email.isBlank() || password.isBlank() -> errorMsg = "Completá los campos"
                                !email.contains("@") -> errorMsg = "Email inválido"
                                password.length < 6 -> errorMsg = "Contraseña muy corta"
                                else -> onLoginSuccess()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Acceso restringido — OLTVI Operations",
                color = Color.White.copy(0.25f),
                fontSize = 11.sp
            )
        }
    }
}
