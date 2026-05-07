package com.devfamily.sehara.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import com.devfamily.sehara.R

val LexendExa = FontFamily(
    Font(R.font.lexend_exa_regular, FontWeight.Normal),
    Font(R.font.lexend_exa_semibold, FontWeight.SemiBold)
)

// Set of Material typography styles to start with
val Typography = Typography(
    // 54sp - Najveći naslov
    displayLarge = TextStyle(
        fontFamily = LexendExa,
        fontWeight = FontWeight.SemiBold,
        fontSize = 54.sp,
        letterSpacing =0.sp
    ),

    displayMedium = TextStyle(
        fontFamily = LexendExa,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        letterSpacing = 3.2.sp
    ),

    titleLarge = TextStyle(
        fontFamily = LexendExa,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),

    titleMedium = TextStyle(
        fontFamily = LexendExa,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp
    ),

    titleSmall = TextStyle(
        fontFamily = LexendExa,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = LexendExa,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = LexendExa,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
)