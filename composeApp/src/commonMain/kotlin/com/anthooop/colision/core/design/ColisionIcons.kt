package com.anthooop.colision.core.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Hand-tuned line icons ported from the design prototype (`docs/design/.../icons.jsx`,
 * 24px viewBox, stroke 1.75, round caps/joins). Kept as stroked [ImageVector]s so we
 * avoid pulling in `material-icons-extended` — the codebase deliberately ships no
 * Material icon dependency. Tinting is handled by the `Icon` composable via its
 * `tint`, so the stroke colour here is just a placeholder.
 */
private fun lineIcon(name: String, vararg paths: String): ImageVector {
    val builder = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )
    paths.forEach { d ->
        builder.addPath(
            pathData = PathParser().parsePathString(d).toNodes(),
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.75f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }
    return builder.build()
}

object ColisionIcons {
    // Agenda — calendar.
    val Calendar: ImageVector by lazy {
        lineIcon(
            "Calendar",
            "M5.5 5H18.5A2.5 2.5 0 0 1 21 7.5V18.5A2.5 2.5 0 0 1 18.5 21H5.5A2.5 2.5 0 0 1 3 18.5V7.5A2.5 2.5 0 0 1 5.5 5Z",
            "M3 9H21",
            "M8 3V7",
            "M16 3V7",
        )
    }

    // Commissions — folder.
    val Folder: ImageVector by lazy {
        lineIcon(
            "Folder",
            "M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z",
        )
    }

    // Sondages — bar chart.
    val Poll: ImageVector by lazy {
        lineIcon("Poll", "M4 20V10", "M10 20V4", "M16 20v-7", "M22 20H2")
    }

    // Membres — two people.
    val Users: ImageVector by lazy {
        lineIcon(
            "Users",
            "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
            "M13 7A4 4 0 1 1 5 7A4 4 0 1 1 13 7Z",
            "M22 21v-2a4 4 0 0 0-3-3.87",
            "M16 3.13a4 4 0 0 1 0 7.75",
        )
    }

    // Projet — single person.
    val User: ImageVector by lazy {
        lineIcon(
            "User",
            "M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2",
            "M16 7A4 4 0 1 1 8 7A4 4 0 1 1 16 7Z",
        )
    }
}
