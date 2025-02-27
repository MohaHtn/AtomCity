package org.arcade.atomcity.ui.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BottomBarPill(
    onHomeClick: () -> Unit
){

    Box(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth())
    {
        Box(
            modifier = Modifier
                .height(40.dp)
                .align(Alignment.Center)
                .width(256.dp)
                .offset(y = (-64).dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick =  onHomeClick,
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        text = "Games"
                    )
                }
                Button(
                    onClick = {  /* Do something */ },
                    modifier = Modifier.height(40.dp)

                ) {
                    Text(
                        text = "Settings"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_8")
@Composable
fun BottomBarPillPreview() {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .offset(y = (64).dp)
    ) {
        BottomBarPill(
            onHomeClick = {}
        )
    }
}