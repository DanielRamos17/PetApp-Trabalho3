package com.example.petapp

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val FundoBege = Color(0xFFF3EEE6)
val VerdeClaro = Color(0xFFE2F4EC)
val VermelhoAlerta = Color(0xFFE04F4F)

@Composable
fun coresCampo() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = VerdePrincipal,
    unfocusedBorderColor = Color.LightGray
)
