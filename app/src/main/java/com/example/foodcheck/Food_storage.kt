
package com.example.foodcheck

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.foodcheck.Room.Refrigerator
import com.example.foodcheck.Room.RefrigeratorProduct
import com.example.foodcheck.internet.Product
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun foodStorage(
    onbackclick: () -> Unit = {},
    productToAdd: Product? = null,
    onProductAdded: () -> Unit = {},
    viewModel: FoodStorageViewModel = koinViewModel()
) {
    val refrigerators by viewModel.refrigerators
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var showAddDialog by remember {
        mutableStateOf(false)
    }

    var placeToDelete by remember {
        mutableStateOf<Refrigerator?>(null)
    }

    var selectedPlace by remember {
        mutableStateOf<Refrigerator?>(null)
    }
    BackHandler {
        if (selectedPlace != null) {
            selectedPlace = null
        } else {
            onbackclick()
        }
    }

    val glassShape = RoundedCornerShape(40.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        Color(0xFF80CFFF),
                        Color.White.copy(alpha = 0.9f)
                    )
                ),
                shape = glassShape
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (selectedPlace == null) {

            // Заголовок и возврат к камере
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {


                Text(
                    text = stringResource(R.string.storage_places_title),
                    fontSize = 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(15.dp))

            // Список мест хранения
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (refrigerators.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(
                                R.string.storage_empty_places
                            )
                        )
                    }
                }

                items(
                    items = refrigerators,
                    key = { it.id }
                ) { refrigerator ->

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.4f))
                            .clickable {
                                if (productToAdd != null) {
                                    viewModel.addScannedProduct(
                                        refrigeratorId = refrigerator.id,
                                        product = productToAdd,
                                        onSaved = onProductAdded
                                    )
                                } else {
                                    selectedPlace = refrigerator
                                }
                            }
                            .padding(12.dp)
                            .heightIn(min = 76.dp)
                    ) {

                        Text(
                            text = refrigerator.name,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .fillMaxWidth()
                                .padding(end = 44.dp, bottom = 35.dp)
                        )

                        IconButton(
                            onClick = {
                                placeToDelete = refrigerator
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.trash_can),
                                contentDescription = stringResource(R.string.storage_delete),
                                modifier = Modifier.size(24.dp),
                                tint = Color.Red
                            )
                        }
                    }
                }
            }

            // Добавление нового места
            Button(
                onClick = {
                    showAddDialog = true
                },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp).border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(20.dp)),elevation = null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    stringResource(
                        R.string.storage_add_place
                    )
                )
            }
            Button(
                onClick = onbackclick,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp).border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(20.dp)),elevation = null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                ),

            ) {
                Text(stringResource(R.string.storage_back), color = Color.Black)
            }


        } else {

            // Содержимое выбранного места
            selectedPlace?.let { place ->
                StorageProducts(
                    place = place,
                    viewModel = viewModel,
                    onBackClick = {
                        selectedPlace = null
                    }
                )
            }
        }
    }

    // Окно добавления места хранения
    if (showAddDialog) {
        StorageNameDialog(
            title = stringResource(
                R.string.storage_new_place
            ),
            onDismiss = {
                showAddDialog = false
            },
            onConfirm = { name ->
                viewModel.addRefrigerator(name)
                showAddDialog = false
            }
        )
    }

    // Подтверждение удаления места
    placeToDelete?.let { place ->
        ConfirmDeleteDialog(
            name = place.name,
            onDismiss = {
                placeToDelete = null
            },
            onConfirm = {
                viewModel.deleteRefrigerator(place.id)
                placeToDelete = null
            }
        )
    }
}

@Composable
fun StorageProducts(
    place: Refrigerator,
    viewModel: FoodStorageViewModel,
    onBackClick: () -> Unit
) {
    val productsFlow = remember(place.id) {
        viewModel.getProducts(place.id)
    }

    val products by productsFlow
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var showAddDialog by remember {
        mutableStateOf(false)
    }

    var productToDelete by remember {
        mutableStateOf<RefrigeratorProduct?>(null)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // Заголовок с кнопкой возврата
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {


            Text(
                text = place.name,
                fontSize = 24.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(15.dp))

        // Список продуктов выбранного места
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            if (products.isEmpty()) {
                item {
                    Text(
                        text = stringResource(
                            R.string.storage_empty_products
                        )
                    )
                }
            }

            items(
                items = products,
                key = { it.id }
            ) { savedProduct ->

                val productName = remember(
                    savedProduct.productJson
                ) {
                    getStoredProductName(savedProduct)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.4f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(12.dp)
                ) {

                    Text(
                        text = productName
                            ?: savedProduct.barcode
                            ?: stringResource(
                                R.string.storage_unnamed_product
                            ),
                        fontSize = 20.sp
                    )

                    TextButton(
                        onClick = {
                            productToDelete = savedProduct
                        }
                    ) {
                        Text(
                            stringResource(
                                R.string.storage_delete
                            )
                        )
                    }
                }
            }
        }

        // Добавление продукта
        Button(
            onClick = {
                showAddDialog = true
            },
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp).border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(20.dp)),elevation = null,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            ),

            ) {
            Text(
                stringResource(
                    R.string.storage_add_product
                )
            )
        }

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp).border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(20.dp)),elevation = null,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            ),

            ) {
            Text(stringResource(R.string.storage_back), color = Color.Black)
        }

    }

    // Окно добавления продукта
    if (showAddDialog) {
        StorageNameDialog(
            title = stringResource(
                R.string.storage_new_product
            ),
            onDismiss = {
                showAddDialog = false
            },
            onConfirm = { name ->
                viewModel.addProduct(place.id, name)
                showAddDialog = false
            }
        )
    }

    // Подтверждение удаления продукта
    productToDelete?.let { savedProduct ->
        ConfirmDeleteDialog(
            name = getStoredProductName(savedProduct)
                ?: stringResource(R.string.storage_product),
            onDismiss = {
                productToDelete = null
            },
            onConfirm = {
                viewModel.deleteProduct(savedProduct.id)
                productToDelete = null
            }
        )
    }
}

// Получаем название продукта из сохранённого JSON
private fun getStoredProductName(
    savedProduct: RefrigeratorProduct
): String? {

    // Пробуем прочитать полный объект Product
    val product = runCatching {
        Json.decodeFromString<Product>(
            savedProduct.productJson
        )
    }.getOrNull()

    val modelName = product?.nameRu
        ?.takeIf { it.isNotBlank() }
        ?: product?.name?.takeIf { it.isNotBlank() }

    if (modelName != null) {
        return modelName
    }

    // Для продуктов, добавленных вручную
    return runCatching {
        val json = Json.parseToJsonElement(
            savedProduct.productJson
        ).jsonObject

        listOf(
            "product_name_ru",
            "product_name",
            "nameRu",
            "name"
        ).firstNotNullOfOrNull { key ->
            json[key]?.jsonPrimitive?.contentOrNull
                ?.takeIf { it.isNotBlank() }
        }
    }.getOrNull()
}
