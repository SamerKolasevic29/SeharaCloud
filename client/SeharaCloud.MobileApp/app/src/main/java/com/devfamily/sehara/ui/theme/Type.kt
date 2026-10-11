package com.devfamily.sehara.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.devfamily.sehara.R

val Lexend = FontFamily(
    Font(R.font.lexend_regular, FontWeight.Normal),
    Font(R.font.lexend_medium, FontWeight.Medium),
    Font(R.font.lexend_semibold, FontWeight.SemiBold),
    Font(R.font.lexend_bold, FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Lexend,
        fontWeight = FontWeight.Bold,
        fontSize = 54.sp,
        letterSpacing = 0.sp
    ),

    displayMedium = TextStyle(
        fontFamily = Lexend,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 3.2.sp
    ),

    titleLarge = TextStyle(
        fontFamily = Lexend,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),

    titleMedium = TextStyle(
        fontFamily = Lexend,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),

    titleSmall = TextStyle(
        fontFamily = Lexend,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = Lexend,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = Lexend,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
)