package org.arcade.atomcity.ui.theme.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.presentation.viewmodel.MainActivityViewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable

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

        if (mainActivityViewModel.isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.padding(paddingValues).fillMaxSize())
        } else {
            LazyColumn {
                items(count = mainActivityViewModel.dataSize) { index ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Replace with actual image or icon
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Item $index",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = "Description for item $index",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Bienvenue Atom City !",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }
        }

//        Card(
//            modifier = Modifier.padding(paddingValues)
//        ){
//            Text(
//                text = "Bienvenue Atom City !",
//                modifier = Modifier.padding(16.dp)
//            )
//        }
    }
}