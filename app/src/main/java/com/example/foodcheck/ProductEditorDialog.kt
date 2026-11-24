
package com.example.foodcheck

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.foodcheck.internet.Product
import com.example.foodcheck.internet.Nutriments

@Composable
fun ProductEditorDialog(
    initialProduct: Product? = null,
    initialBarcode: String? = null,
    initialCount: Int = 1,
    onDismiss: () -> Unit,
    onSave: (Product, String?, Int) -> Unit
) {
    var nameRu by remember {
        mutableStateOf(initialProduct?.nameRu.orEmpty())
    }

    var name by remember {
        mutableStateOf(initialProduct?.name.orEmpty())
    }

    var brand by remember {
        mutableStateOf(initialProduct?.brand.orEmpty())
    }

    var quantity by remember {
        mutableStateOf(initialProduct?.quantity.orEmpty())
    }

    var barcode by remember {
        mutableStateOf(initialBarcode.orEmpty())
    }

    var count by remember {
        mutableStateOf(initialCount.toString())
    }

    var imageUrl by remember {
        mutableStateOf(initialProduct?.imageUrl.orEmpty())
    }

    var nutriScore by remember {
        mutableStateOf(initialProduct?.nutriScore.orEmpty())
    }

    var ingredientsRu by remember {
        mutableStateOf(initialProduct?.ingredientsRu.orEmpty())
    }

    var ingredients by remember {
        mutableStateOf(initialProduct?.ingredients.orEmpty())
    }

    var allergens by remember {
        mutableStateOf(
            initialProduct?.allergens?.joinToString(", ").orEmpty()
        )
    }

    var calories by remember {
        mutableStateOf(
            initialProduct?.nutriments?.calories?.toString().orEmpty()
        )
    }

    var proteins by remember {
        mutableStateOf(
            initialProduct?.nutriments?.proteins?.toString().orEmpty()
        )
    }

    var fats by remember {
        mutableStateOf(
            initialProduct?.nutriments?.fats?.toString().orEmpty()
        )
    }

    var carbohydrates by remember {
        mutableStateOf(
            initialProduct?.nutriments?.carbohydrates?.toString().orEmpty()
        )
    }

    var sugars by remember {
        mutableStateOf(
            initialProduct?.nutriments?.sugars?.toString().orEmpty()
        )
    }

    var salt by remember {
        mutableStateOf(
            initialProduct?.nutriments?.salt?.toString().orEmpty()
        )
    }

    val numbers = listOf(
        calories, proteins, fats, carbohydrates, sugars, salt
    )

    val validNumbers = numbers.all { value ->
        value.isBlank() ||
                value.trim().replace(',', '.')
                    .toDoubleOrNull()
                    ?.let { it >= 0.0 } == true
    }

    val validCount = (count.toIntOrNull() ?: 0) > 0

    val canSave = (name.isNotBlank() || nameRu.isNotBlank()) &&
            validNumbers && validCount

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = stringResource(
                        if (initialProduct == null)
                            R.string.storage_new_product
                        else
                            R.string.storage_edit_product
                    ),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    ProductFormField(
                        value = nameRu,
                        onValueChange = { nameRu = it },
                        label = stringResource(R.string.product_details_name)
                    )

                    ProductFormField(
                        value = name,
                        onValueChange = { name = it },
                        label = stringResource(
                            R.string.product_details_original_name
                        )
                    )

                    ProductFormField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = stringResource(
                            R.string.product_details_manufacturer
                        )
                    )

                    ProductFormField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = stringResource(
                            R.string.product_details_quantity
                        )
                    )

                    ProductFormField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = stringResource(R.string.storage_barcode)
                    )

                    ProductFormField(
                        value = count,
                        onValueChange = { count = it },
                        label = stringResource(R.string.storage_count),
                        numeric = true
                    )

                    ProductFormField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = stringResource(R.string.storage_image_url)
                    )

                    ProductFormField(
                        value = nutriScore,
                        onValueChange = { nutriScore = it },
                        label = stringResource(
                            R.string.product_details_nutri_score
                        )
                    )

                    ProductFormField(
                        value = ingredientsRu,
                        onValueChange = { ingredientsRu = it },
                        label = stringResource(
                            R.string.product_details_ingredients_ru
                        ),
                        multiline = true
                    )

                    ProductFormField(
                        value = ingredients,
                        onValueChange = { ingredients = it },
                        label = stringResource(
                            R.string.product_details_ingredients
                        ),
                        multiline = true
                    )

                    ProductFormField(
                        value = allergens,
                        onValueChange = { allergens = it },
                        label = stringResource(
                            R.string.product_details_allergens
                        ),
                        multiline = true
                    )

                    Text(
                        text = stringResource(
                            R.string.Nutritional_value_per_100_g
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    ProductFormField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = stringResource(R.string.calories),
                        numeric = true
                    )

                    ProductFormField(
                        value = proteins,
                        onValueChange = { proteins = it },
                        label = stringResource(R.string.proteins),
                        numeric = true
                    )

                    ProductFormField(
                        value = fats,
                        onValueChange = { fats = it },
                        label = stringResource(R.string.fats),
                        numeric = true
                    )

                    ProductFormField(
                        value = carbohydrates,
                        onValueChange = { carbohydrates = it },
                        label = stringResource(R.string.carbohydrates),
                        numeric = true
                    )

                    ProductFormField(
                        value = sugars,
                        onValueChange = { sugars = it },
                        label = stringResource(
                            R.string.product_details_sugar
                        ),
                        numeric = true
                    )

                    ProductFormField(
                        value = salt,
                        onValueChange = { salt = it },
                        label = stringResource(
                            R.string.product_details_salt
                        ),
                        numeric = true
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.storage_cancel), color = Color(0xFF80CFFF))
                    }

                    Button(
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF80CFFF),
                            contentColor = Color.Black),
                        onClick = {

                            fun optional(text: String): String? {
                                return text.trim()
                                    .takeIf { it.isNotBlank() }
                            }

                            fun number(text: String): Double? {
                                return text.trim()
                                    .replace(',', '.')
                                    .toDoubleOrNull()
                            }

                            val nutriments =
                                if (numbers.all { it.isBlank() }) {
                                    null
                                } else {
                                    Nutriments(
                                        calories = number(calories),
                                        proteins = number(proteins),
                                        fats = number(fats),
                                        carbohydrates = number(carbohydrates),
                                        sugars = number(sugars),
                                        salt = number(salt)
                                    )
                                }

                            val product = Product(
                                name = optional(name),
                                nameRu = optional(nameRu),
                                brand = optional(brand),
                                quantity = optional(quantity),
                                imageUrl = optional(imageUrl),
                                ingredients = optional(ingredients),
                                ingredientsRu = optional(ingredientsRu),
                                allergens = allergens
                                    .split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() }
                                    .takeIf { it.isNotEmpty() },
                                nutriScore = optional(nutriScore)
                                    ?.uppercase(),
                                nutriments = nutriments
                            )

                            onSave(
                                product,
                                optional(barcode),
                                count.toInt()
                            )
                        }
                    ) {
                        Text(stringResource(R.string.storage_save))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    numeric: Boolean = false,
    multiline: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = !multiline,
        minLines = if (multiline) 2 else 1,
        maxLines = if (multiline) 4 else 1,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (numeric)
                KeyboardType.Decimal
            else
                KeyboardType.Text
        ),
        isError = numeric &&
                value.isNotBlank() &&
                value.trim().replace(',', '.')
                    .toDoubleOrNull() == null
    )
}
