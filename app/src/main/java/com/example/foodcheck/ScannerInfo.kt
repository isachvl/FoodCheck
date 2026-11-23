package com.example.foodcheck

import android.widget.Toast
import coil3.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodcheck.ui.theme.nullColor


@Composable
fun ScannerInfo( product: Product, onbackclick:() -> Unit = {}){
    val glassShape = RoundedCornerShape(40.dp)
    val showDetails = remember {
        mutableStateOf(false)
    }
    if (showDetails.value) {
        ProductDetails(
            product = product,
            onBackClick = {
                showDetails.value = false
            }
        )
        return
    }
    Column( modifier = Modifier.background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.9f),
                Color(0xFF80CFFF).copy(alpha = 1f),
                Color.White.copy(alpha = 0.9f)
            )
        ),
        shape = glassShape
    )
        .fillMaxSize()
        .padding(20.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier) {

            val imageUrl = product.imageUrl?.takeIf { it.isNotBlank() }


            val imageFailed = remember(imageUrl) {
                mutableStateOf(false)
            }

            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {

                if (imageUrl == null || imageFailed.value) {

                    // Нет фотографии — показываем категорию
                    NutriScoreBadge(product.nutriScore, null)

                } else {

                    // Есть фотография — показываем её
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        onError = {
                            imageFailed.value = true
                        }
                    )

                    // Категория поверх фотографии справа снизу
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        NutriScoreBadge(product.nutriScore, imageUrl)
                    }
                }
            }
            }
            Text(
                text = product.nameRu ?.takeIf { it.isNotBlank() }
                    ?: product.name ?: stringResource(R.string.Error),fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
            )
            Text(
                text = listOfNotNull(
                    product.brand,
                    product.quantity
                ).joinToString(" | "),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )


            Text(
                text =stringResource(R.string.Nutritional_value_per_100_g),
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row( modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IntemCart(stringResource(R.string.calories), product.nutriments?.calories?.toString())
                    IntemCart(stringResource(R.string.proteins), product.nutriments?.proteins?.toString())


                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IntemCart(stringResource(R.string.fats), product.nutriments?.fats?.toString())
                    IntemCart(stringResource(R.string.carbohydrates), product.nutriments?.carbohydrates?.toString())
                }
            }

            Text(
                text = product.ingredientsRu
                    ?.takeIf { it.isNotBlank() }
                    ?: product.ingredients
                    ?: "Состав не указан",
                fontSize = 13.sp,
                maxLines = 3
            )
            val interactionSource = remember {
                MutableInteractionSource()
            }
            val isPressed = interactionSource.collectIsPressedAsState()
            Text(
                text = stringResource(R.string.More_info),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline,

                color = if (isPressed.value) Color.Blue else Color.Black,
                modifier = Modifier.clickable(interactionSource = interactionSource,indication = ripple()){showDetails.value = true



                }
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {Button(onClick = {
            },
                modifier = Modifier


                    .size(width = 300.dp, height = 60.dp)


                    .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(20.dp)),elevation = null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                ),
            ) { Text(stringResource(R.string.addINfridge), fontSize = 20.sp)}
            Button(onClick = {onbackclick()
            },
                modifier = Modifier


                    .size(width = 300.dp, height = 60.dp)


                    .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(20.dp)),elevation = null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                ),
            ) { Text(stringResource(R.string.back), fontSize = 20.sp)}
        }}

            }




fun nutriScoreColor(score: String?): Color {
    return when (score?.lowercase()) {
        "a" -> Color(0x801B8F3A)   // зелёный
        "b" -> Color(0x806DBE45)   // светло-зелёный
        "c" -> Color(0x80FFC107)   // жёлтый
        "d" -> Color(0x80FF9800)   // оранжевый
        "e" -> Color(0x80E53935)   // красный
        else -> nullColor
    }
}
@Composable
fun IntemCart(heading: String, info: String?){

    Column(
        modifier = Modifier
            .size(width = 150.dp, height = 60.dp)
            .padding(2.dp)
            .border(width = 2.dp, color = Color.Gray, shape = RoundedCornerShape(20.dp)),horizontalAlignment = Alignment.CenterHorizontally
        ,verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = heading,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = info
                ?.takeIf { it.isNotBlank() }
                ?: "-",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
        )
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScannerInfoPreview() {
    ScannerInfo(
        product = Product(
            name = "Nutella",
            brand = "Ferrero",
            quantity = "400 г",
            ingredients = "Сахар, фундук, какао, молоко",
            nutriments = Nutriments(
                calories = 539.0,
                proteins = 6.3,
                fats = 30.9,
                carbohydrates = 57.5
            )
        )
    )
}
@Composable
fun NutriScoreBadge(score: String?,imageUrl: String?) {
    val value = score?.uppercase() ?: "?"
    val color = nutriScoreColor(score)

    Box(
        modifier = Modifier

            .background(color = color, shape = RoundedCornerShape(14.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp).then(if (imageUrl == null) Modifier.fillMaxSize() else Modifier),contentAlignment = Alignment.Center

    ) {
        Text(
            text = "$value",
            color = if (imageUrl == null) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = if (imageUrl == null) 50.sp else 16.sp
        )



    }}


