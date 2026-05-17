package com.anthooop.colision.core.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Radius scale mapped from tokens.jsx (`R` object: xs=6, sm=10, md=14, lg=18,
// xl=24, pill=999). Material3 has 5 shape slots; pill is exposed separately
// as `pill` for the FAB/chip use case.
val colisionShapes: Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

val pillShape: RoundedCornerShape = RoundedCornerShape(percent = 50)
