package com.example.foodcheck

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodcheck.internet.Product
import kotlinx.coroutines.launch

@Composable
fun ModalNavigationDrawer() {
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )
    val showProductInfo = remember {
        mutableStateOf(false)
    }
    val productToAdd = remember {
        mutableStateOf<Product?>(null)
    }
    val scope = rememberCoroutineScope()
    var showStorage = remember {
        mutableStateOf(false)
    }

    val glassShape = RoundedCornerShape(30.dp)

    ModalNavigationDrawer(

        drawerState = drawerState, gesturesEnabled = !showProductInfo.value && !showStorage.value,
        drawerContent = {
            ModalDrawerSheet( modifier = Modifier
                .systemBarsPadding()
                .background(
                    brush = linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color(0xFF80CFFF).copy(alpha = 0.6f),
                            Color.White.copy(alpha = 0.3f)
                        )
                    ), shape = RoundedCornerShape(topStart = 0.dp,
                        bottomStart = 0.dp,
                        topEnd = 30.dp,
                        bottomEnd = 30.dp)
                ).fillMaxHeight()
                .border(
                    width = 1.dp,
                    brush = linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.5f),
                            Color.White.copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 0.dp,
                        bottomStart = 0.dp,
                        topEnd = 30.dp,
                        bottomEnd = 30.dp)
                ),drawerContainerColor = Color.Transparent  ) {

                Button(
                    onClick = {
                        showStorage.value = true

                        scope.launch {
                            drawerState.close()
                        }

                    },

                    modifier = Modifier.padding(top = 10.dp)

                        .fillMaxWidth().height(45.dp)

                        ,border  = BorderStroke(
                            width = 2.dp,
                            brush = linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.9f),
                                    Color.White.copy(alpha = 0.6f)
                                )
                            )),
                            shape = glassShape,

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF80CFFF),
                        contentColor = Color.White
                    ),


                    ) {
                    Text(stringResource(R.string.Grocery_accounting), fontSize = 20.sp,fontWeight = FontWeight.Bold)
                }


                Text("")
            }
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()) {

            MainScreen(
                showStorage = showStorage.value,
                showProductInfo = showProductInfo,
                onChooseStorage = { product ->
                    productToAdd.value = product
                    showStorage.value = true
                }
            )

            if (showStorage.value) {

                Box(

                    modifier = Modifier
                        .fillMaxHeight(0.85f)
                        .align(Alignment.Center)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center

                ) {
                    foodStorage(
                        productToAdd = productToAdd.value,

                        onProductAdded = {
                            productToAdd.value = null
                            showStorage.value = false
                        },

                        onbackclick = {
                            productToAdd.value = null
                            showStorage.value = false
                        }
                    )}



            }
            if (!showStorage.value &&
                !showProductInfo.value &&
                drawerState.currentValue == DrawerValue.Closed
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },

                    modifier = Modifier
                        .padding(10.dp)
                        .background(
                            brush = linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    Color(0xFF80CFFF).copy(alpha = 0.20f),
                                    Color.White.copy(alpha = 0.10f)
                                )
                            ),
                            shape = RoundedCornerShape(40.dp)
                        )
                        .border(
                            width = 1.dp,
                            brush = linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.85f),
                                    Color.White.copy(alpha = 0.9f)
                                )
                            ),
                            shape = RoundedCornerShape(30.dp)
                        )
                        .size(50.dp)
                        .align(Alignment.TopStart), colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "☰",
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
