
package com.example.foodcheck

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodcheck.internet.Product

@Composable
fun ProductDetails(
    product: Product,
    onBackClick: () -> Unit
) {
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
                shape = RoundedCornerShape(40.dp)
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {

        Text(
            text = stringResource(R.string.product_details_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Text(
                text = stringResource(R.string.product_details_basic_info),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            DetailItem(
                stringResource(R.string.product_details_name),
                product.nameRu ?: product.name
            )

            DetailItem(
                stringResource(R.string.product_details_original_name),
                product.name
            )

            DetailItem(
                stringResource(R.string.product_details_manufacturer),
                product.brand
            )

            DetailItem(
                stringResource(R.string.product_details_quantity),
                product.quantity
            )

            DetailItem(
                stringResource(R.string.product_details_nutri_score),
                product.nutriScore?.uppercase()
            )

            Text(
                text = stringResource(R.string.Nutritional_value_per_100_g),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            val nutrition = product.nutriments

            DetailItem(
                stringResource(R.string.calories),
                nutrition?.calories?.let {
                    stringResource(R.string.product_details_kcal_value, it.toString())
                }
            )

            DetailItem(
                stringResource(R.string.proteins),
                nutrition?.proteins?.let {
                    stringResource(R.string.product_details_grams_value, it.toString())
                }
            )



            DetailItem(
                stringResource(R.string.fats),
                nutrition?.fats?.let {
                    stringResource(R.string.product_details_grams_value, it.toString())
                }
            )

            DetailItem(
                stringResource(R.string.carbohydrates),
                nutrition?.carbohydrates?.let {
                    stringResource(R.string.product_details_grams_value, it.toString())
                }
            )

            DetailItem(
                stringResource(R.string.product_details_sugar),
                nutrition?.sugars?.let {
                    stringResource(R.string.product_details_grams_value, it.toString())
                }
            )

            DetailItem(
                stringResource(R.string.product_details_salt),
                nutrition?.salt?.let {
                    stringResource(R.string.product_details_grams_value, it.toString())
                }
            )

            Text(
                text = stringResource(R.string.product_details_ingredients_heading),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            DetailItem(
                stringResource(R.string.product_details_ingredients_ru),
                product.ingredientsRu
            )
            DetailItem(
                stringResource(R.string.product_details_ingredients),
                product.ingredientsRu
                    ?.takeIf { it.isNotBlank() }
                    ?: product.ingredients
            )

            DetailItem(
                stringResource(R.string.product_details_allergens),
                product.allergens
                    ?.takeIf { it.isNotEmpty() }
                    ?.joinToString(", ") {
                        it.substringAfter(":")
                    }
            )
        }

        Button(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .border(
                    width = 2.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = stringResource(R.string.back),
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun DetailItem(
    title: String,
    value: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray
        )

        Text(
            text = value?.takeIf { it.isNotBlank() }
                ?: stringResource(R.string.product_details_not_specified),
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}
