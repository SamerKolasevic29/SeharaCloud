package com.devfamily.sehara.ui.screens.home

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.components.CategoryCard
import androidx.compose.material3.MaterialTheme

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.padding(start = 24.dp, top = 50.dp, end = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    colorFilter = ColorFilter.tint(Color(0xFF3A1012))
                )
                Spacer(modifier = Modifier.width(32.dp))
                Text(
                    text = "Sehara",
                    color = Color(0xFF8E8D8D),
                    style = MaterialTheme.typography.displayMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CategoryCard(
                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                    backgroundImage = R.drawable.home_video_card,
                    icon = R.drawable.ic_video,
                    label = "Video",
                    onClick = {}
                )
                CategoryCard(
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                    backgroundImage = R.drawable.home_music_card,
                    icon = R.drawable.ic_music,
                    label = "Music",
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CategoryCard(
                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                    backgroundImage = R.drawable.home_photo_card,
                    icon = R.drawable.ic_photo,
                    label = "Photo",
                    onClick = {}
                )
                CategoryCard (
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                    backgroundImage = R.drawable.home_docs_card,
                    icon = R.drawable.ic_docs,
                    label = "Docs",
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { (context as? Activity)?.finish() },
                modifier = Modifier
                    .padding(bottom = 40.dp)
                    .fillMaxWidth(0.4f)
                    .height(46.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(150.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F001E)),

            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_exit),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = "Exit",
                    color = Color.White,
                    style= MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}
