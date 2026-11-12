package androidx.compose.material.icons.automirrored.filled

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

private fun autoMirroredIcon(name: String, block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit): ImageVector =
    materialIcon(name = name, autoMirror = true) {
        materialPath(pathBuilder = block)
    }

val Icons.AutoMirrored.Filled.ArrowForwardIos: ImageVector by lazy {
    autoMirroredIcon("AutoMirrored.Filled.ArrowForwardIos") {
        moveTo(6.23f, 20.23f)
        lineTo(8.0f, 22.0f)
        lineToRelative(10.0f, -10.0f)
        lineTo(8.0f, 2.0f)
        lineTo(6.23f, 3.77f)
        lineTo(14.46f, 12.0f)
        close()
    }
}

val Icons.AutoMirrored.Filled.VolumeUp: ImageVector by lazy {
    autoMirroredIcon("AutoMirrored.Filled.VolumeUp") {
        moveTo(3.0f, 9.0f)
        verticalLineToRelative(6.0f)
        horizontalLineToRelative(4.0f)
        lineToRelative(5.0f, 5.0f)
        verticalLineTo(4.0f)
        lineTo(7.0f, 9.0f)
        horizontalLineTo(3.0f)
        close()
        moveTo(16.5f, 12.0f)
        curveToRelative(0.0f, -1.77f, -1.02f, -3.29f, -2.5f, -4.03f)
        verticalLineToRelative(8.05f)
        curveToRelative(1.48f, -0.73f, 2.5f, -2.25f, 2.5f, -4.02f)
        close()
        moveTo(14.0f, 3.23f)
        verticalLineToRelative(2.06f)
        curveToRelative(2.89f, 0.86f, 5.0f, 3.54f, 5.0f, 6.71f)
        reflectiveCurveToRelative(-2.11f, 5.85f, -5.0f, 6.71f)
        verticalLineToRelative(2.06f)
        curveToRelative(4.01f, -0.91f, 7.0f, -4.49f, 7.0f, -8.77f)
        reflectiveCurveToRelative(-2.99f, -7.86f, -7.0f, -8.77f)
        close()
    }
}

val Icons.AutoMirrored.Filled.VolumeOff: ImageVector by lazy {
    autoMirroredIcon("AutoMirrored.Filled.VolumeOff") {
        moveTo(16.5f, 12.0f)
        curveToRelative(0.0f, -1.77f, -1.02f, -3.29f, -2.5f, -4.03f)
        verticalLineToRelative(2.21f)
        lineToRelative(2.45f, 2.45f)
        curveToRelative(0.03f, -0.2f, 0.05f, -0.41f, 0.05f, -0.63f)
        close()
        moveTo(19.0f, 12.0f)
        curveToRelative(0.0f, 0.94f, -0.2f, 1.82f, -0.54f, 2.64f)
        lineToRelative(1.51f, 1.51f)
        curveTo(20.63f, 14.91f, 21.0f, 13.5f, 21.0f, 12.0f)
        curveToRelative(0.0f, -4.28f, -2.99f, -7.86f, -7.0f, -8.77f)
        verticalLineToRelative(2.06f)
        curveToRelative(2.89f, 0.86f, 5.0f, 3.54f, 5.0f, 6.71f)
        close()
        moveTo(4.27f, 3.0f)
        lineTo(3.0f, 4.27f)
        lineTo(7.73f, 9.0f)
        horizontalLineTo(3.0f)
        verticalLineToRelative(6.0f)
        horizontalLineToRelative(4.0f)
        lineToRelative(5.0f, 5.0f)
        verticalLineToRelative(-6.73f)
        lineToRelative(4.25f, 4.25f)
        curveToRelative(-0.67f, 0.52f, -1.42f, 0.93f, -2.25f, 1.18f)
        verticalLineToRelative(2.06f)
        curveToRelative(1.38f, -0.31f, 2.63f, -0.95f, 3.69f, -1.81f)
        lineTo(19.73f, 21.0f)
        lineTo(21.0f, 19.73f)
        lineToRelative(-9.0f, -9.0f)
        lineTo(4.27f, 3.0f)
        close()
        moveTo(12.0f, 4.0f)
        lineTo(9.91f, 6.09f)
        lineTo(12.0f, 8.18f)
        verticalLineTo(4.0f)
        close()
    }
}

val Icons.AutoMirrored.Filled.MenuBook: ImageVector by lazy {
    autoMirroredIcon("AutoMirrored.Filled.MenuBook") {
        moveTo(21.0f, 5.0f)
        curveToRelative(-1.11f, -0.35f, -2.33f, -0.5f, -3.5f, -0.5f)
        curveToRelative(-1.95f, 0.0f, -4.05f, 0.4f, -5.5f, 1.5f)
        curveToRelative(-1.45f, -1.1f, -3.55f, -1.5f, -5.5f, -1.5f)
        reflectiveCurveTo(2.45f, 4.9f, 1.0f, 6.0f)
        verticalLineToRelative(14.65f)
        curveToRelative(0.0f, 0.25f, 0.25f, 0.5f, 0.5f, 0.5f)
        curveToRelative(0.1f, 0.0f, 0.15f, -0.05f, 0.25f, -0.05f)
        curveTo(3.1f, 20.45f, 5.05f, 20.0f, 6.5f, 20.0f)
        curveToRelative(1.95f, 0.0f, 4.05f, 0.4f, 5.5f, 1.5f)
        curveToRelative(1.35f, -0.85f, 3.8f, -1.5f, 5.5f, -1.5f)
        curveToRelative(1.65f, 0.0f, 3.35f, 0.3f, 4.75f, 1.05f)
        curveToRelative(0.1f, 0.05f, 0.15f, 0.05f, 0.25f, 0.05f)
        curveToRelative(0.25f, 0.0f, 0.5f, -0.25f, 0.5f, -0.5f)
        verticalLineTo(6.0f)
        curveToRelative(-0.6f, -0.45f, -1.25f, -0.75f, -2.0f, -1.0f)
        close()
        moveTo(21.0f, 18.5f)
        curveToRelative(-1.1f, -0.35f, -2.3f, -0.5f, -3.5f, -0.5f)
        curveToRelative(-1.7f, 0.0f, -4.15f, 0.65f, -5.5f, 1.5f)
        verticalLineTo(8.0f)
        curveToRelative(1.35f, -0.85f, 3.8f, -1.5f, 5.5f, -1.5f)
        curveToRelative(1.2f, 0.0f, 2.4f, 0.15f, 3.5f, 0.5f)
        verticalLineToRelative(11.5f)
        close()
    }
}
