package org.arcade.atomcity.ui.theme.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.presentation.viewmodel.MainActivityViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(mainActivityViewModel: MainActivityViewModel) {

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = "Atom City") },
                navigationIcon = { },
                actions = { }
            )
        },
        bottomBar = {
            BottomAppBar (
                modifier = Modifier.padding(1.dp)
            ){
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f)

                ) {
                    Text(text = "Home")
                }
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Settings")
                }
            }
        }
    ) { paddingValues ->

        mainActivityViewModel.fetchData()

        Card(
            modifier = Modifier.padding(paddingValues)
        ){
            Text(
                text = "Bienvenue Atom City !",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}