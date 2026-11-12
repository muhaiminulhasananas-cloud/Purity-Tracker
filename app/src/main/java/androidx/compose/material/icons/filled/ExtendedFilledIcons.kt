package androidx.compose.material.icons.filled

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook as AutoMenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeOff as AutoVolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp as AutoVolumeUp
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.material.icons.outlined.Bedtime as OutlinedBedtime
import androidx.compose.material.icons.outlined.Block as OutlinedBlock
import androidx.compose.material.icons.outlined.Book as OutlinedBook
import androidx.compose.material.icons.outlined.BookmarkBorder as OutlinedBookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline as OutlinedChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteForever as OutlinedDeleteForever
import androidx.compose.material.icons.outlined.EmojiEvents as OutlinedEmojiEvents
import androidx.compose.material.icons.outlined.Key as OutlinedKey
import androidx.compose.material.icons.outlined.Language as OutlinedLanguage
import androidx.compose.material.icons.outlined.People as OutlinedPeople
import androidx.compose.material.icons.outlined.PowerSettingsNew as OutlinedPowerSettingsNew
import androidx.compose.material.icons.outlined.Schedule as OutlinedSchedule
import androidx.compose.material.icons.outlined.Shield as OutlinedShield
import androidx.compose.material.icons.outlined.Tune as OutlinedTune
import androidx.compose.material.icons.outlined.VerifiedUser as OutlinedVerifiedUser
import androidx.compose.material.icons.outlined.Visibility as OutlinedVisibility
import androidx.compose.material.icons.outlined.VisibilityOff as OutlinedVisibilityOff
import androidx.compose.material.icons.outlined.WarningAmber as OutlinedWarningAmber
import androidx.compose.material.icons.outlined.WbSunny as OutlinedWbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder

private fun filledIcon(name: String, block: PathBuilder.() -> Unit): ImageVector =
    materialIcon(name = name) {
        materialPath(pathBuilder = block)
    }

val Icons.Filled.Bedtime: ImageVector get() = Icons.Outlined.OutlinedBedtime
val Icons.Filled.Block: ImageVector get() = Icons.Outlined.OutlinedBlock
val Icons.Filled.Book: ImageVector get() = Icons.Outlined.OutlinedBook
val Icons.Filled.BookmarkBorder: ImageVector get() = Icons.Outlined.OutlinedBookmarkBorder
val Icons.Filled.ChatBubbleOutline: ImageVector get() = Icons.Outlined.OutlinedChatBubbleOutline
val Icons.Filled.DeleteForever: ImageVector get() = Icons.Outlined.OutlinedDeleteForever
val Icons.Filled.EmojiEvents: ImageVector get() = Icons.Outlined.OutlinedEmojiEvents
val Icons.Filled.Key: ImageVector get() = Icons.Outlined.OutlinedKey
val Icons.Filled.Language: ImageVector get() = Icons.Outlined.OutlinedLanguage
val Icons.Filled.MenuBook: ImageVector get() = Icons.AutoMirrored.Filled.AutoMenuBook
val Icons.Filled.People: ImageVector get() = Icons.Outlined.OutlinedPeople
val Icons.Filled.PowerSettingsNew: ImageVector get() = Icons.Outlined.OutlinedPowerSettingsNew
val Icons.Filled.Schedule: ImageVector get() = Icons.Outlined.OutlinedSchedule
val Icons.Filled.Shield: ImageVector get() = Icons.Outlined.OutlinedShield
val Icons.Filled.Tune: ImageVector get() = Icons.Outlined.OutlinedTune
val Icons.Filled.VerifiedUser: ImageVector get() = Icons.Outlined.OutlinedVerifiedUser
val Icons.Filled.Visibility: ImageVector get() = Icons.Outlined.OutlinedVisibility
val Icons.Filled.VisibilityOff: ImageVector get() = Icons.Outlined.OutlinedVisibilityOff
val Icons.Filled.VolumeOff: ImageVector get() = Icons.AutoMirrored.Filled.AutoVolumeOff
val Icons.Filled.VolumeUp: ImageVector get() = Icons.AutoMirrored.Filled.AutoVolumeUp
val Icons.Filled.WarningAmber: ImageVector get() = Icons.Outlined.OutlinedWarningAmber
val Icons.Filled.WbSunny: ImageVector get() = Icons.Outlined.OutlinedWbSunny

val Icons.Filled.AccessTime: ImageVector get() = Icons.Outlined.OutlinedSchedule
val Icons.Filled.Alarm: ImageVector get() = Icons.Outlined.OutlinedSchedule

val Icons.Filled.AcUnit: ImageVector by lazy {
    filledIcon("Filled.AcUnit") {
        moveTo(22.0f, 11.0f)
        horizontalLineToRelative(-4.17f)
        lineToRelative(3.24f, -3.24f)
        lineToRelative(-1.41f, -1.42f)
        lineTo(15.0f, 11.0f)
        horizontalLineToRelative(-2.0f)
        verticalLineTo(9.0f)
        lineToRelative(4.66f, -4.66f)
        lineToRelative(-1.42f, -1.41f)
        lineTo(13.0f, 6.17f)
        verticalLineTo(2.0f)
        horizontalLineToRelative(-2.0f)
        verticalLineToRelative(4.17f)
        lineTo(7.76f, 2.93f)
        lineTo(6.34f, 4.34f)
        lineTo(11.0f, 9.0f)
        verticalLineToRelative(2.0f)
        horizontalLineTo(9.0f)
        lineTo(4.34f, 6.34f)
        lineTo(2.93f, 7.76f)
        lineTo(6.17f, 11.0f)
        horizontalLineTo(2.0f)
        verticalLineToRelative(2.0f)
        horizontalLineToRelative(4.17f)
        lineToRelative(-3.24f, 3.24f)
        lineToRelative(1.41f, 1.42f)
        lineTo(9.0f, 13.0f)
        horizontalLineToRelative(2.0f)
        verticalLineToRelative(2.0f)
        lineToRelative(-4.66f, 4.66f)
        lineToRelative(1.42f, 1.41f)
        lineTo(11.0f, 17.83f)
        verticalLineTo(22.0f)
        horizontalLineToRelative(2.0f)
        verticalLineToRelative(-4.17f)
        lineToRelative(3.24f, 3.24f)
        lineToRelative(1.42f, -1.41f)
        lineTo(13.0f, 15.0f)
        verticalLineToRelative(-2.0f)
        horizontalLineToRelative(2.0f)
        lineToRelative(4.66f, 4.66f)
        lineToRelative(1.41f, -1.42f)
        lineTo(17.83f, 13.0f)
        horizontalLineTo(22.0f)
        close()
    }
}

val Icons.Filled.AddAlert: ImageVector by lazy {
    filledIcon("Filled.AddAlert") {
        moveTo(12.0f, 22.0f)
        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        horizontalLineToRelative(-4.0f)
        curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
        close()
        moveTo(18.0f, 16.0f)
        verticalLineToRelative(-5.0f)
        curveToRelative(0.0f, -3.07f, -1.63f, -5.64f, -4.5f, -6.32f)
        verticalLineTo(4.0f)
        curveToRelative(0.0f, -0.83f, -0.67f, -1.5f, -1.5f, -1.5f)
        reflectiveCurveToRelative(-1.5f, 0.67f, -1.5f, 1.5f)
        verticalLineToRelative(0.68f)
        curveTo(7.64f, 5.36f, 6.0f, 7.92f, 6.0f, 11.0f)
        verticalLineToRelative(5.0f)
        lineToRelative(-2.0f, 2.0f)
        verticalLineToRelative(1.0f)
        horizontalLineToRelative(16.0f)
        verticalLineToRelative(-1.0f)
        lineToRelative(-2.0f, -2.0f)
        close()
    }
}

val Icons.Filled.AddPhotoAlternate: ImageVector by lazy {
    filledIcon("Filled.AddPhotoAlternate") {
        moveTo(19.0f, 7.0f)
        verticalLineToRelative(2.99f)
        reflectiveCurveToRelative(-1.99f, 0.01f, -2.0f, 0.0f)
        verticalLineTo(7.0f)
        horizontalLineToRelative(-3.0f)
        reflectiveCurveToRelative(0.01f, -1.99f, 0.0f, -2.0f)
        horizontalLineToRelative(3.0f)
        verticalLineTo(2.0f)
        horizontalLineToRelative(2.0f)
        verticalLineToRelative(3.0f)
        horizontalLineToRelative(3.0f)
        verticalLineToRelative(2.0f)
        horizontalLineToRelative(-3.0f)
        close()
        moveTo(16.0f, 11.0f)
        verticalLineTo(8.0f)
        horizontalLineToRelative(-3.0f)
        verticalLineTo(5.0f)
        horizontalLineTo(5.0f)
        curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
        verticalLineToRelative(12.0f)
        curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
        horizontalLineToRelative(12.0f)
        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        verticalLineToRelative(-8.0f)
        horizontalLineToRelative(-3.0f)
        close()
        moveTo(5.0f, 19.0f)
        lineToRelative(3.0f, -4.0f)
        lineToRelative(2.0f, 3.0f)
        lineToRelative(3.0f, -4.0f)
        lineToRelative(4.0f, 5.0f)
        horizontalLineTo(5.0f)
        close()
    }
}

val Icons.Filled.AlternateEmail: ImageVector by lazy {
    filledIcon("Filled.AlternateEmail") {
        moveTo(12.0f, 2.0f)
        curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
        reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
        horizontalLineToRelative(5.0f)
        verticalLineToRelative(-2.0f)
        horizontalLineToRelative(-5.0f)
        curveToRelative(-4.34f, 0.0f, -8.0f, -3.66f, -8.0f, -8.0f)
        reflectiveCurveToRelative(3.66f, -8.0f, 8.0f, -8.0f)
        reflectiveCurveToRelative(8.0f, 3.66f, 8.0f, 8.0f)
        verticalLineToRelative(1.43f)
        curveToRelative(0.0f, 0.79f, -0.71f, 1.57f, -1.5f, 1.57f)
        reflectiveCurveToRelative(-1.5f, -0.78f, -1.5f, -1.57f)
        verticalLineTo(12.0f)
        curveToRelative(0.0f, -2.76f, -2.24f, -5.0f, -5.0f, -5.0f)
        reflectiveCurveToRelative(-5.0f, 2.24f, -5.0f, 5.0f)
        reflectiveCurveToRelative(2.24f, 5.0f, 5.0f, 5.0f)
        curveToRelative(1.38f, 0.0f, 2.64f, -0.56f, 3.54f, -1.47f)
        curveToRelative(0.65f, 0.89f, 1.77f, 1.47f, 2.96f, 1.47f)
        curveToRelative(1.97f, 0.0f, 3.5f, -1.6f, 3.5f, -3.57f)
        verticalLineTo(12.0f)
        curveToRelative(0.0f, -5.52f, -4.48f, -10.0f, -10.0f, -10.0f)
        close()
        moveTo(12.0f, 15.0f)
        curveToRelative(-1.66f, 0.0f, -3.0f, -1.34f, -3.0f, -3.0f)
        reflectiveCurveToRelative(1.34f, -3.0f, 3.0f, -3.0f)
        reflectiveCurveToRelative(3.0f, 1.34f, 3.0f, 3.0f)
        reflectiveCurveToRelative(-1.34f, 3.0f, -3.0f, 3.0f)
        close()
    }
}

val Icons.Filled.AutoAwesome: ImageVector by lazy {
    filledIcon("Filled.AutoAwesome") {
        moveTo(19.0f, 9.0f)
        lineToRelative(1.25f, -2.75f)
        lineTo(23.0f, 5.0f)
        lineToRelative(-2.75f, -1.25f)
        lineTo(19.0f, 1.0f)
        lineToRelative(-1.25f, 2.75f)
        lineTo(15.0f, 5.0f)
        lineToRelative(2.75f, 1.25f)
        lineTo(19.0f, 9.0f)
        close()
        moveTo(11.5f, 9.5f)
        lineTo(9.0f, 4.0f)
        lineTo(6.5f, 9.5f)
        lineTo(1.0f, 12.0f)
        lineToRelative(5.5f, 2.5f)
        lineTo(9.0f, 20.0f)
        lineToRelative(2.5f, -5.5f)
        lineTo(17.0f, 12.0f)
        lineToRelative(-5.5f, -2.5f)
        close()
        moveTo(19.0f, 15.0f)
        lineToRelative(-1.25f, 2.75f)
        lineTo(15.0f, 19.0f)
        lineToRelative(2.75f, 1.25f)
        lineTo(19.0f, 23.0f)
        lineToRelative(1.25f, -2.75f)
        lineTo(23.0f, 19.0f)
        lineToRelative(-2.75f, -1.25f)
        lineTo(19.0f, 15.0f)
        close()
    }
}

val Icons.Filled.Badge: ImageVector by lazy {
    filledIcon("Filled.Badge") {
        moveTo(20.0f, 7.0f)
        horizontalLineToRelative(-5.0f)
        verticalLineTo(4.0f)
        curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
        horizontalLineToRelative(-2.0f)
        curveTo(9.9f, 2.0f, 9.0f, 2.9f, 9.0f, 4.0f)
        verticalLineToRelative(3.0f)
        horizontalLineTo(4.0f)
        curveTo(2.9f, 7.0f, 2.0f, 7.9f, 2.0f, 9.0f)
        verticalLineToRelative(11.0f)
        curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
        horizontalLineToRelative(16.0f)
        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        verticalLineTo(9.0f)
        curveTo(22.0f, 7.9f, 21.1f, 7.0f, 20.0f, 7.0f)
        close()
        moveTo(9.0f, 12.0f)
        curveToRelative(0.83f, 0.0f, 1.5f, 0.67f, 1.5f, 1.5f)
        reflectiveCurveTo(9.83f, 15.0f, 9.0f, 15.0f)
        reflectiveCurveToRelative(-1.5f, -0.67f, -1.5f, -1.5f)
        reflectiveCurveTo(8.17f, 12.0f, 9.0f, 12.0f)
        close()
        moveTo(12.0f, 18.0f)
        horizontalLineTo(6.0f)
        verticalLineToRelative(-0.75f)
        curveToRelative(0.0f, -1.0f, 2.0f, -1.5f, 3.0f, -1.5f)
        reflectiveCurveToRelative(3.0f, 0.5f, 3.0f, 1.5f)
        verticalLineTo(18.0f)
        close()
        moveTo(13.0f, 9.0f)
        horizontalLineToRelative(-2.0f)
        verticalLineTo(4.0f)
        horizontalLineToRelative(2.0f)
        verticalLineTo(9.0f)
        close()
        moveTo(18.0f, 16.5f)
        horizontalLineToRelative(-4.0f)
        verticalLineTo(15.0f)
        horizontalLineToRelative(4.0f)
        verticalLineTo(16.5f)
        close()
        moveTo(18.0f, 13.5f)
        horizontalLineToRelative(-4.0f)
        verticalLineTo(12.0f)
        horizontalLineToRelative(4.0f)
        verticalLineTo(13.5f)
        close()
    }
}

val Icons.Filled.Bolt: ImageVector by lazy {
    filledIcon("Filled.Bolt") {
        moveTo(11.0f, 21.0f)
        horizontalLineToRelative(-1.0f)
        lineToRelative(1.0f, -7.0f)
        horizontalLineTo(7.5f)
        curveToRelative(-0.58f, 0.0f, -0.57f, -0.32f, -0.38f, -0.66f)
        lineToRelative(0.1f, -0.16f)
        lineTo(12.92f, 3.0f)
        horizontalLineToRelative(1.0f)
        lineToRelative(-1.0f, 7.0f)
        horizontalLineToRelative(3.5f)
        curveToRelative(0.49f, 0.0f, 0.56f, 0.33f, 0.47f, 0.51f)
        lineToRelative(-0.07f, 0.15f)
        lineTo(11.0f, 21.0f)
        close()
    }
}

val Icons.Filled.Bookmark: ImageVector by lazy {
    filledIcon("Filled.Bookmark") {
        moveTo(17.0f, 3.0f)
        horizontalLineTo(7.0f)
        curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
        lineTo(5.0f, 21.0f)
        lineToRelative(7.0f, -3.0f)
        lineToRelative(7.0f, 3.0f)
        verticalLineTo(5.0f)
        curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
        close()
    }
}

val Icons.Filled.Cake: ImageVector by lazy {
    filledIcon("Filled.Cake") {
        moveTo(12.0f, 6.0f)
        curveToRelative(1.11f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        curveToRelative(0.0f, -0.38f, -0.1f, -0.73f, -0.29f, -1.03f)
        lineTo(12.0f, 0.0f)
        lineToRelative(-1.71f, 2.97f)
        curveTo(10.1f, 3.27f, 10.0f, 3.62f, 10.0f, 4.0f)
        curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
        close()
        moveTo(18.0f, 9.0f)
        horizontalLineToRelative(-5.0f)
        verticalLineTo(7.0f)
        horizontalLineToRelative(-2.0f)
        verticalLineToRelative(2.0f)
        horizontalLineTo(6.0f)
        curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
        verticalLineToRelative(9.0f)
        curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
        horizontalLineToRelative(16.0f)
        curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
        verticalLineToRelative(-9.0f)
        curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
        close()
    }
}

val Icons.Filled.CalendarMonth: ImageVector by lazy {
    filledIcon("Filled.CalendarMonth") {
        moveTo(19.0f, 4.0f)
        horizontalLineToRelative(-1.0f)
        verticalLineTo(2.0f)
        horizontalLineToRelative(-2.0f)
        verticalLineToRelative(2.0f)
        horizontalLineTo(8.0f)
        verticalLineTo(2.0f)
        horizontalLineTo(6.0f)
        verticalLineToRelative(2.0f)
        horizontalLineTo(5.0f)
        curveToRelative(-1.11f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
        lineTo(3.0f, 20.0f)
        curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
        horizontalLineToRelative(14.0f)
        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        verticalLineTo(6.0f)
        curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
        close()
        moveTo(19.0f, 20.0f)
        horizontalLineTo(5.0f)
        verticalLineTo(10.0f)
        horizontalLineToRelative(14.0f)
        verticalLineToRelative(10.0f)
        close()
    }
}

val Icons.Filled.CalendarToday: ImageVector get() = Icons.Filled.CalendarMonth

val Icons.Filled.CallEnd: ImageVector by lazy {
    filledIcon("Filled.CallEnd") {
        moveTo(12.0f, 9.0f)
        curveToRelative(-1.6f, 0.0f, -3.15f, 0.25f, -4.6f, 0.72f)
        verticalLineToRelative(3.1f)
        curveToRelative(0.0f, 0.39f, -0.23f, 0.74f, -0.56f, 0.9f)
        curveToRelative(-0.98f, 0.49f, -1.87f, 1.12f, -2.66f, 1.85f)
        curveToRelative(-0.18f, 0.18f, -0.43f, 0.28f, -0.7f, 0.28f)
        curveToRelative(-0.28f, 0.0f, -0.53f, -0.11f, -0.71f, -0.29f)
        lineTo(0.29f, 13.08f)
        curveToRelative(-0.18f, -0.17f, -0.29f, -0.42f, -0.29f, -0.7f)
        curveToRelative(0.0f, -0.28f, 0.11f, -0.53f, 0.29f, -0.71f)
        curveTo(3.34f, 8.78f, 7.46f, 7.0f, 12.0f, 7.0f)
        reflectiveCurveToRelative(8.66f, 1.78f, 11.71f, 4.67f)
        curveToRelative(0.18f, 0.18f, 0.29f, 0.43f, 0.29f, 0.71f)
        curveToRelative(0.0f, 0.28f, -0.11f, 0.53f, -0.29f, 0.71f)
        lineToRelative(-2.48f, 2.48f)
        curveToRelative(-0.18f, 0.18f, -0.43f, 0.29f, -0.71f, 0.29f)
        curveToRelative(-0.27f, 0.0f, -0.52f, -0.11f, -0.7f, -0.28f)
        curveToRelative(-0.79f, -0.74f, -1.69f, -1.36f, -2.67f, -1.85f)
        curveToRelative(-0.33f, -0.16f, -0.56f, -0.5f, -0.56f, -0.9f)
        verticalLineToRelative(-3.1f)
        curveTo(15.15f, 9.25f, 13.6f, 9.0f, 12.0f, 9.0f)
        close()
    }
}

val Icons.Filled.ChatBubble: ImageVector by lazy {
    filledIcon("Filled.ChatBubble") {
        moveTo(20.0f, 2.0f)
        horizontalLineTo(4.0f)
        curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
        verticalLineToRelative(18.0f)
        lineToRelative(4.0f, -4.0f)
        horizontalLineToRelative(14.0f)
        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        verticalLineTo(4.0f)
        curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
        close()
    }
}

val Icons.Filled.ChevronRight: ImageVector by lazy {
    filledIcon("Filled.ChevronRight") {
        moveTo(10.0f, 6.0f)
        lineTo(8.59f, 7.41f)
        lineTo(13.17f, 12.0f)
        lineToRelative(-4.58f, 4.59f)
        lineTo(10.0f, 18.0f)
        lineToRelative(6.0f, -6.0f)
        close()
    }
}

val Icons.Filled.ContentCopy: ImageVector by lazy {
    filledIcon("Filled.ContentCopy") {
        moveTo(16.0f, 1.0f)
        horizontalLineTo(4.0f)
        curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
        verticalLineToRelative(14.0f)
        horizontalLineToRelative(2.0f)
        verticalLineTo(3.0f)
        horizontalLineToRelative(12.0f)
        verticalLineTo(1.0f)
        close()
        moveTo(19.0f, 5.0f)
        horizontalLineTo(8.0f)
        curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
        verticalLineToRelative(14.0f)
        curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
        horizontalLineToRelative(11.0f)
        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        verticalLineTo(7.0f)
        curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
        close()
        moveTo(19.0f, 21.0f)
        horizontalLineTo(8.0f)
        verticalLineTo(7.0f)
        horizontalLineToRelative(11.0f)
        verticalLineToRelative(14.0f)
        close()
    }
}

val Icons.Filled.DeleteOutline: ImageVector by lazy {
    filledIcon("Filled.DeleteOutline") {
        moveTo(6.0f, 19.0f)
        curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
        horizontalLineToRelative(8.0f)
        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        verticalLineTo(7.0f)
        horizontalLineTo(6.0f)
        verticalLineToRelative(12.0f)
        close()
        moveTo(8.0f, 9.0f)
        horizontalLineToRelative(8.0f)
        verticalLineToRelative(10.0f)
        horizontalLineTo(8.0f)
        verticalLineTo(9.0f)
        close()
        moveTo(15.5f, 4.0f)
        lineToRelative(-1.0f, -1.0f)
        horizontalLineToRelative(-5.0f)
        lineToRelative(-1.0f, 1.0f)
        horizontalLineTo(5.0f)
        verticalLineToRelative(2.0f)
        horizontalLineToRelative(14.0f)
        verticalLineTo(4.0f)
        horizontalLineToRelative(-3.5f)
        close()
    }
}

val Icons.Filled.DoneAll: ImageVector by lazy {
    filledIcon("Filled.DoneAll") {
        moveTo(18.0f, 7.0f)
        lineToRelative(-1.41f, -1.41f)
        lineToRelative(-6.34f, 6.34f)
        lineToRelative(1.41f, 1.41f)
        lineTo(18.0f, 7.0f)
        close()
        moveTo(22.24f, 5.59f)
        lineTo(11.66f, 16.17f)
        lineTo(7.48f, 12.0f)
        lineToRelative(-1.41f, 1.41f)
        lineTo(11.66f, 19.0f)
        lineToRelative(12.0f, -12.0f)
        lineToRelative(-1.42f, -1.41f)
        close()
        moveTo(0.41f, 13.41f)
        lineTo(6.0f, 19.0f)
        lineToRelative(1.41f, -1.41f)
        lineTo(1.83f, 12.0f)
        lineTo(0.41f, 13.41f)
        close()
    }
}

val Icons.Filled.Explore: ImageVector by lazy {
    filledIcon("Filled.Explore") {
        moveTo(12.0f, 10.9f)
        curveToRelative(-0.61f, 0.0f, -1.1f, 0.49f, -1.1f, 1.1f)
        reflectiveCurveToRelative(0.49f, 1.1f, 1.1f, 1.1f)
        curveToRelative(0.61f, 0.0f, 1.1f, -0.49f, 1.1f, -1.1f)
        reflectiveCurveToRelative(-0.49f, -1.1f, -1.1f, -1.1f)
        close()
        moveTo(12.0f, 2.0f)
        curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
        reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
        reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
        reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
        close()
        moveTo(14.19f, 14.19f)
        lineTo(6.0f, 18.0f)
        lineToRelative(3.81f, -8.19f)
        lineTo(18.0f, 6.0f)
        lineToRelative(-3.81f, 8.19f)
        close()
    }
}

val Icons.Filled.FitnessCenter: ImageVector by lazy {
    filledIcon("Filled.FitnessCenter") {
        moveTo(20.57f, 14.86f)
        lineTo(22.0f, 13.43f)
        lineTo(20.57f, 12.0f)
        lineTo(17.0f, 15.57f)
        lineTo(8.43f, 7.0f)
        lineTo(12.0f, 3.43f)
        lineTo(10.57f, 2.0f)
        lineTo(9.14f, 3.43f)
        lineTo(7.71f, 2.0f)
        lineTo(5.57f, 4.14f)
        lineTo(4.14f, 2.71f)
        lineTo(2.71f, 4.14f)
        lineToRelative(1.43f, 1.43f)
        lineTo(2.0f, 7.71f)
        lineToRelative(1.43f, 1.43f)
        lineTo(2.0f, 10.57f)
        lineTo(3.43f, 12.0f)
        lineTo(7.0f, 8.43f)
        lineTo(15.57f, 17.0f)
        lineTo(12.0f, 20.57f)
        lineTo(13.43f, 22.0f)
        lineToRelative(1.43f, -1.43f)
        lineTo(16.29f, 22.0f)
        lineToRelative(2.14f, -2.14f)
        lineToRelative(1.43f, 1.43f)
        lineToRelative(1.43f, -1.43f)
        lineToRelative(-1.43f, -1.43f)
        lineTo(22.0f, 16.29f)
        close()
    }
}

val Icons.Filled.Groups: ImageVector get() = Icons.Outlined.OutlinedPeople

val Icons.Filled.Hd: ImageVector by lazy {
    filledIcon("Filled.Hd") {
        moveTo(19.0f, 3.0f)
        horizontalLineTo(5.0f)
        curveToRelative(-1.11f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
        verticalLineToRelative(14.0f)
        curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
        horizontalLineToRelative(14.0f)
        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        verticalLineTo(5.0f)
        curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
        close()
        moveTo(11.0f, 15.0f)
        horizontalLineTo(9.5f)
        verticalLineToRelative(-2.0f)
        horizontalLineToRelative(-2.0f)
        verticalLineToRelative(2.0f)
        horizontalLineTo(6.0f)
        verticalLineTo(9.0f)
        horizontalLineToRelative(1.5f)
        verticalLineToRelative(2.5f)
        horizontalLineToRelative(2.0f)
        verticalLineTo(9.0f)
        horizontalLineTo(11.0f)
        verticalLineToRelative(6.0f)
        close()
        moveTo(13.0f, 9.0f)
        horizontalLineToRelative(4.0f)
        curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
        verticalLineToRelative(4.0f)
        curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
        horizontalLineToRelative(-4.0f)
        verticalLineTo(9.0f)
        close()
        moveTo(14.5f, 13.5f)
        horizontalLineToRelative(2.0f)
        verticalLineToRelative(-3.0f)
        horizontalLineToRelative(-2.0f)
        verticalLineToRelative(3.0f)
        close()
    }
}

val Icons.Filled.HeadsetMic: ImageVector by lazy {
    filledIcon("Filled.HeadsetMic") {
        moveTo(12.0f, 1.0f)
        curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
        verticalLineToRelative(7.0f)
        curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
        horizontalLineToRelative(3.0f)
        verticalLineToRelative(-8.0f)
        horizontalLineTo(5.0f)
        verticalLineToRelative(-2.0f)
        curveToRelative(0.0f, -3.87f, 3.13f, -7.0f, 7.0f, -7.0f)
        reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
        verticalLineToRelative(2.0f)
        horizontalLineToRelative(-4.0f)
        verticalLineToRelative(8.0f)
        horizontalLineToRelative(4.0f)
        verticalLineToRelative(1.0f)
        horizontalLineToRelative(-7.0f)
        verticalLineToRelative(2.0f)
        horizontalLineToRelative(6.0f)
        curveToRelative(1.66f, 0.0f, 3.0f, -1.34f, 3.0f, -3.0f)
        verticalLineTo(10.0f)
        curveToRelative(0.0f, -4.97f, -4.03f, -9.0f, -9.0f, -9.0f)
        close()
    }
}

val Icons.Filled.History: ImageVector by lazy {
    filledIcon("Filled.History") {
        moveTo(13.0f, 3.0f)
        curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
        horizontalLineTo(1.0f)
        lineToRelative(3.89f, 3.89f)
        lineToRelative(0.07f, 0.14f)
        lineTo(9.0f, 12.0f)
        horizontalLineTo(6.0f)
        curveToRelative(0.0f, -3.87f, 3.13f, -7.0f, 7.0f, -7.0f)
        reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
        reflectiveCurveToRelative(-3.13f, 7.0f, -7.0f, 7.0f)
        curveToRelative(-1.93f, 0.0f, -3.68f, -0.79f, -4.94f, -2.06f)
        lineToRelative(-1.42f, 1.42f)
        curveTo(8.27f, 19.99f, 10.51f, 21.0f, 13.0f, 21.0f)
        curveToRelative(4.97f, 0.0f, 9.0f, -4.03f, 9.0f, -9.0f)
        reflectiveCurveToRelative(-4.03f, -9.0f, -9.0f, -9.0f)
        close()
        moveTo(12.0f, 8.0f)
        verticalLineToRelative(5.0f)
        lineToRelative(4.28f, 2.54f)
        lineToRelative(0.72f, -1.21f)
        lineToRelative(-3.5f, -2.08f)
        verticalLineTo(8.0f)
        horizontalLineTo(12.0f)
        close()
    }
}

val Icons.Filled.HourglassTop: ImageVector by lazy {
    filledIcon("Filled.HourglassTop") {
        moveTo(6.0f, 2.0f)
        lineToRelative(0.01f, 6.0f)
        lineTo(10.0f, 12.0f)
        lineToRelative(-3.99f, 4.01f)
        lineTo(6.0f, 22.0f)
        horizontalLineToRelative(12.0f)
        verticalLineToRelative(-5.99f)
        lineTo(14.0f, 12.0f)
        lineToRelative(3.99f, -4.01f)
        lineTo(18.0f, 2.0f)
        horizontalLineTo(6.0f)
        close()
    }
}

val Icons.Filled.Image: ImageVector by lazy {
    filledIcon("Filled.Image") {
        moveTo(21.0f, 19.0f)
        verticalLineTo(5.0f)
        curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
        horizontalLineTo(5.0f)
        curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
        verticalLineToRelative(14.0f)
        curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
        horizontalLineToRelative(14.0f)
        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        close()
        moveTo(8.5f, 13.5f)
        lineToRelative(2.5f, 3.01f)
        lineTo(14.5f, 12.0f)
        lineToRelative(4.5f, 6.0f)
        horizontalLineTo(5.0f)
        lineToRelative(3.5f, -4.5f)
        close()
    }
}

val Icons.Filled.Leaderboard: ImageVector by lazy {
    filledIcon("Filled.Leaderboard") {
        moveTo(7.5f, 21.0f)
        horizontalLineTo(2.0f)
        verticalLineTo(9.0f)
        horizontalLineToRelative(5.5f)
        verticalLineToRelative(12.0f)
        close()
        moveTo(14.75f, 3.0f)
        horizontalLineToRelative(-5.5f)
        verticalLineToRelative(18.0f)
        horizontalLineToRelative(5.5f)
        verticalLineTo(3.0f)
        close()
        moveTo(22.0f, 11.0f)
        horizontalLineToRelative(-5.5f)
        verticalLineToRelative(10.0f)
        horizontalLineTo(22.0f)
        verticalLineTo(11.0f)
        close()
    }
}

val Icons.Filled.Link: ImageVector by lazy {
    filledIcon("Filled.Link") {
        moveTo(3.9f, 12.0f)
        curveToRelative(0.0f, -1.71f, 1.39f, -3.1f, 3.1f, -3.1f)
        horizontalLineToRelative(4.0f)
        verticalLineTo(7.0f)
        horizontalLineTo(7.0f)
        curveToRelative(-2.76f, 0.0f, -5.0f, 2.24f, -5.0f, 5.0f)
        reflectiveCurveToRelative(2.24f, 5.0f, 5.0f, 5.0f)
        horizontalLineToRelative(4.0f)
        verticalLineToRelative(-1.9f)
        horizontalLineTo(7.0f)
        curveToRelative(-1.71f, 0.0f, -3.1f, -1.39f, -3.1f, -3.1f)
        close()
        moveTo(8.0f, 13.0f)
        horizontalLineToRelative(8.0f)
        verticalLineToRelative(-2.0f)
        horizontalLineTo(8.0f)
        verticalLineToRelative(2.0f)
        close()
        moveTo(17.0f, 7.0f)
        horizontalLineToRelative(-4.0f)
        verticalLineToRelative(1.9f)
        horizontalLineToRelative(4.0f)
        curveToRelative(1.71f, 0.0f, 3.1f, 1.39f, 3.1f, 3.1f)
        reflectiveCurveToRelative(-1.39f, 3.1f, -3.1f, 3.1f)
        horizontalLineToRelative(-4.0f)
        verticalLineTo(17.0f)
        horizontalLineToRelative(4.0f)
        curveToRelative(2.76f, 0.0f, 5.0f, -2.24f, 5.0f, -5.0f)
        reflectiveCurveToRelative(-2.24f, -5.0f, -5.0f, -5.0f)
        close()
    }
}

val Icons.Filled.LocalDrink: ImageVector by lazy {
    filledIcon("Filled.LocalDrink") {
        moveTo(3.0f, 2.0f)
        lineToRelative(2.01f, 18.23f)
        curveTo(5.13f, 21.23f, 5.97f, 22.0f, 7.0f, 22.0f)
        horizontalLineToRelative(10.0f)
        curveToRelative(1.03f, 0.0f, 1.87f, -0.77f, 1.99f, -1.77f)
        lineTo(21.0f, 2.0f)
        horizontalLineTo(3.0f)
        close()
        moveTo(12.0f, 19.0f)
        curveToRelative(-1.66f, 0.0f, -3.0f, -1.34f, -3.0f, -3.0f)
        curveToRelative(0.0f, -2.0f, 3.0f, -5.4f, 3.0f, -5.4f)
        reflectiveCurveToRelative(3.0f, 3.4f, 3.0f, 5.4f)
        curveToRelative(0.0f, 1.66f, -1.34f, 3.0f, -3.0f, 3.0f)
        close()
    }
}

val Icons.Filled.LockReset: ImageVector get() = Icons.Outlined.OutlinedKey

val Icons.Filled.MarkEmailRead: ImageVector by lazy {
    filledIcon("Filled.MarkEmailRead") {
        moveTo(20.0f, 4.0f)
        horizontalLineTo(4.0f)
        curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
        lineTo(2.0f, 18.0f)
        curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
        horizontalLineToRelative(16.0f)
        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        verticalLineTo(6.0f)
        curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
        close()
        moveTo(20.0f, 8.0f)
        lineToRelative(-8.0f, 5.0f)
        lineTo(4.0f, 8.0f)
        verticalLineTo(6.0f)
        lineToRelative(8.0f, 5.0f)
        lineToRelative(8.0f, -5.0f)
        verticalLineToRelative(2.0f)
        close()
    }
}

val Icons.Filled.MarkEmailUnread: ImageVector get() = Icons.Filled.MarkEmailRead

val Icons.Filled.Mic: ImageVector by lazy {
    filledIcon("Filled.Mic") {
        moveTo(12.0f, 14.0f)
        curveToRelative(1.66f, 0.0f, 2.99f, -1.34f, 2.99f, -3.0f)
        lineTo(15.0f, 5.0f)
        curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
        reflectiveCurveTo(9.0f, 3.34f, 9.0f, 5.0f)
        verticalLineToRelative(6.0f)
        curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
        close()
        moveTo(17.3f, 11.0f)
        curveToRelative(0.0f, 3.0f, -2.54f, 5.1f, -5.3f, 5.1f)
        reflectiveCurveTo(6.7f, 14.0f, 6.7f, 11.0f)
        horizontalLineTo(5.0f)
        curveToRelative(0.0f, 3.41f, 2.72f, 6.23f, 6.0f, 6.72f)
        verticalLineTo(21.0f)
        horizontalLineToRelative(2.0f)
        verticalLineToRelative(-3.28f)
        curveToRelative(3.28f, -0.48f, 6.0f, -3.3f, 6.0f, -6.72f)
        horizontalLineToRelative(-1.7f)
        close()
    }
}

val Icons.Filled.MicOff: ImageVector by lazy {
    filledIcon("Filled.MicOff") {
        moveTo(19.0f, 11.0f)
        horizontalLineToRelative(-1.7f)
        curveToRelative(0.0f, 0.74f, -0.16f, 1.43f, -0.43f, 2.05f)
        lineToRelative(1.23f, 1.23f)
        curveToRelative(0.56f, -0.98f, 0.9f, -2.09f, 0.9f, -3.28f)
        close()
        moveTo(14.98f, 11.17f)
        curveToRelative(0.0f, -0.06f, 0.02f, -0.11f, 0.02f, -0.17f)
        verticalLineTo(5.0f)
        curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
        reflectiveCurveTo(9.0f, 3.34f, 9.0f, 5.0f)
        verticalLineToRelative(0.18f)
        lineToRelative(5.98f, 5.99f)
        close()
        moveTo(4.27f, 3.0f)
        lineTo(3.0f, 4.27f)
        lineToRelative(6.01f, 6.01f)
        verticalLineTo(11.0f)
        curveToRelative(0.0f, 1.66f, 1.33f, 3.0f, 2.99f, 3.0f)
        curveToRelative(0.22f, 0.0f, 0.44f, -0.03f, 0.65f, -0.08f)
        lineToRelative(1.66f, 1.66f)
        curveToRelative(-0.71f, 0.33f, -1.5f, 0.52f, -2.31f, 0.52f)
        curveToRelative(-2.76f, 0.0f, -5.3f, -2.1f, -5.3f, -5.1f)
        horizontalLineTo(5.0f)
        curveToRelative(0.0f, 3.41f, 2.72f, 6.23f, 6.0f, 6.72f)
        verticalLineTo(21.0f)
        horizontalLineToRelative(2.0f)
        verticalLineToRelative(-3.28f)
        curveToRelative(0.91f, -0.13f, 1.77f, -0.45f, 2.54f, -0.9f)
        lineTo(19.73f, 21.0f)
        lineTo(21.0f, 19.73f)
        lineTo(4.27f, 3.0f)
        close()
    }
}

val Icons.Filled.Mood: ImageVector by lazy {
    filledIcon("Filled.Mood") {
        moveTo(11.99f, 2.0f)
        curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
        reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
        curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
        reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
        close()
        moveTo(12.0f, 20.0f)
        curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
        reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
        reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
        reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
        close()
        moveTo(15.5f, 11.0f)
        curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
        reflectiveCurveTo(16.33f, 8.0f, 15.5f, 8.0f)
        reflectiveCurveTo(14.0f, 8.67f, 14.0f, 9.5f)
        reflectiveCurveToRelative(0.67f, 1.5f, 1.5f, 1.5f)
        close()
        moveTo(8.5f, 11.0f)
        curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
        reflectiveCurveTo(9.33f, 8.0f, 8.5f, 8.0f)
        reflectiveCurveTo(7.0f, 8.67f, 7.0f, 9.5f)
        reflectiveCurveTo(7.67f, 11.0f, 8.5f, 11.0f)
        close()
        moveTo(12.0f, 17.5f)
        curveToRelative(2.33f, 0.0f, 4.31f, -1.46f, 5.11f, -3.5f)
        horizontalLineTo(6.89f)
        curveToRelative(0.8f, 2.04f, 2.78f, 3.5f, 5.11f, 3.5f)
        close()
    }
}

val Icons.Filled.MoreHoriz: ImageVector by lazy {
    filledIcon("Filled.MoreHoriz") {
        moveTo(6.0f, 10.0f)
        curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
        reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
        reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
        reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
        close()
        moveTo(18.0f, 10.0f)
        curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
        reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
        reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
        reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
        close()
        moveTo(12.0f, 10.0f)
        curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
        reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
        reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
        reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
        close()
    }
}

val Icons.Filled.Mosque: ImageVector by lazy {
    filledIcon("Filled.Mosque") {
        moveTo(24.0f, 7.0f)
        curveToRelative(0.0f, -1.1f, -2.0f, -3.0f, -2.0f, -3.0f)
        reflectiveCurveToRelative(-2.0f, 1.9f, -2.0f, 3.0f)
        curveToRelative(0.0f, 0.74f, 0.4f, 1.38f, 1.0f, 1.72f)
        verticalLineTo(13.0f)
        horizontalLineToRelative(-2.0f)
        verticalLineToRelative(-2.0f)
        curveToRelative(0.0f, -2.42f, -1.72f, -4.44f, -4.0f, -4.9f)
        verticalLineTo(4.0f)
        horizontalLineToRelative(-2.0f)
        verticalLineToRelative(2.1f)
        curveTo(10.72f, 6.56f, 9.0f, 8.58f, 9.0f, 11.0f)
        verticalLineToRelative(2.0f)
        horizontalLineTo(7.0f)
        verticalLineTo(8.72f)
        curveTo(7.6f, 8.38f, 8.0f, 7.74f, 8.0f, 7.0f)
        curveToRelative(0.0f, -1.1f, -2.0f, -3.0f, -2.0f, -3.0f)
        reflectiveCurveTo(4.0f, 5.9f, 4.0f, 7.0f)
        curveToRelative(0.0f, 0.74f, 0.4f, 1.38f, 1.0f, 1.72f)
        verticalLineTo(21.0f)
        horizontalLineToRelative(7.0f)
        verticalLineToRelative(-4.0f)
        curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
        reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
        verticalLineToRelative(4.0f)
        horizontalLineToRelative(7.0f)
        verticalLineTo(8.72f)
        curveTo(23.6f, 8.38f, 24.0f, 7.74f, 24.0f, 7.0f)
        close()
    }
}

val Icons.Filled.MusicNote: ImageVector by lazy {
    filledIcon("Filled.MusicNote") {
        moveTo(12.0f, 3.0f)
        verticalLineToRelative(10.55f)
        curveToRelative(-0.59f, -0.34f, -1.27f, -0.55f, -2.0f, -0.55f)
        curveToRelative(-2.21f, 0.0f, -4.0f, 1.79f, -4.0f, 4.0f)
        reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
        reflectiveCurveToRelative(4.0f, -1.79f, 4.0f, -4.0f)
        verticalLineTo(7.0f)
        horizontalLineToRelative(4.0f)
        verticalLineTo(3.0f)
        horizontalLineToRelative(-6.0f)
        close()
    }
}

val Icons.Filled.MyLocation: ImageVector by lazy {
    filledIcon("Filled.MyLocation") {
        moveTo(12.0f, 8.0f)
        curveToRelative(-2.21f, 0.0f, -4.0f, 1.79f, -4.0f, 4.0f)
        reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
        reflectiveCurveToRelative(4.0f, -1.79f, 4.0f, -4.0f)
        reflectiveCurveToRelative(-1.79f, -4.0f, -4.0f, -4.0f)
        close()
        moveTo(20.94f, 11.0f)
        curveToRelative(-0.46f, -4.17f, -3.77f, -7.48f, -7.94f, -7.94f)
        verticalLineTo(1.0f)
        horizontalLineToRelative(-2.0f)
        verticalLineToRelative(2.06f)
        curveTo(6.83f, 3.52f, 3.52f, 6.83f, 3.06f, 11.0f)
        horizontalLineTo(1.0f)
        verticalLineToRelative(2.0f)
        horizontalLineToRelative(2.06f)
        curveToRelative(0.46f, 4.17f, 3.77f, 7.48f, 7.94f, 7.94f)
        verticalLineTo(23.0f)
        horizontalLineToRelative(2.0f)
        verticalLineToRelative(-2.06f)
        curveToRelative(4.17f, -0.46f, 7.48f, -3.77f, 7.94f, -7.94f)
        horizontalLineTo(23.0f)
        verticalLineToRelative(-2.0f)
        horizontalLineToRelative(-2.06f)
        close()
    }
}

val Icons.Filled.NightsStay: ImageVector get() = Icons.Outlined.OutlinedBedtime

val Icons.Filled.NotificationsActive: ImageVector get() = Icons.Filled.AddAlert

val Icons.Filled.NotificationsOff: ImageVector by lazy {
    filledIcon("Filled.NotificationsOff") {
        moveTo(20.0f, 18.69f)
        lineTo(7.84f, 6.14f)
        lineTo(5.27f, 3.49f)
        lineTo(4.0f, 4.76f)
        lineToRelative(2.8f, 2.8f)
        verticalLineToRelative(0.01f)
        curveToRelative(-0.52f, 0.99f, -0.8f, 2.16f, -0.8f, 3.42f)
        verticalLineToRelative(5.0f)
        lineToRelative(-2.0f, 2.0f)
        verticalLineToRelative(1.0f)
        horizontalLineToRelative(13.73f)
        lineToRelative(2.0f, 2.0f)
        lineTo(21.0f, 19.72f)
        lineToRelative(-1.0f, -1.03f)
        close()
        moveTo(12.0f, 22.0f)
        curveToRelative(1.11f, 0.0f, 2.0f, -0.89f, 2.0f, -2.0f)
        horizontalLineToRelative(-4.0f)
        curveToRelative(0.0f, 1.11f, 0.89f, 2.0f, 2.0f, 2.0f)
        close()
    }
}

val Icons.Filled.Opacity: ImageVector get() = Icons.Filled.LocalDrink

val Icons.Filled.Pause: ImageVector by lazy {
    filledIcon("Filled.Pause") {
        moveTo(6.0f, 19.0f)
        horizontalLineToRelative(4.0f)
        verticalLineTo(5.0f)
        horizontalLineTo(6.0f)
        verticalLineToRelative(14.0f)
        close()
        moveTo(14.0f, 5.0f)
        verticalLineToRelative(14.0f)
        horizontalLineToRelative(4.0f)
        verticalLineTo(5.0f)
        horizontalLineToRelative(-4.0f)
        close()
    }
}

val Icons.Filled.PersonAdd: ImageVector by lazy {
    filledIcon("Filled.PersonAdd") {
        moveTo(15.0f, 12.0f)
        curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
        reflectiveCurveToRelative(-1.79f, -4.0f, -4.0f, -4.0f)
        reflectiveCurveToRelative(-4.0f, 1.79f, -4.0f, 4.0f)
        reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
        close()
        moveTo(6.0f, 10.0f)
        verticalLineTo(7.0f)
        horizontalLineTo(4.0f)
        verticalLineToRelative(3.0f)
        horizontalLineTo(1.0f)
        verticalLineToRelative(2.0f)
        horizontalLineToRelative(3.0f)
        verticalLineToRelative(3.0f)
        horizontalLineToRelative(2.0f)
        verticalLineToRelative(-3.0f)
        horizontalLineToRelative(3.0f)
        verticalLineToRelative(-2.0f)
        horizontalLineTo(6.0f)
        close()
        moveTo(15.0f, 14.0f)
        curveToRelative(-2.67f, 0.0f, -8.0f, 1.34f, -8.0f, 4.0f)
        verticalLineToRelative(2.0f)
        horizontalLineToRelative(16.0f)
        verticalLineToRelative(-2.0f)
        curveToRelative(0.0f, -2.66f, -5.33f, -4.0f, -8.0f, -4.0f)
        close()
    }
}

val Icons.Filled.PhotoCamera: ImageVector by lazy {
    filledIcon("Filled.PhotoCamera") {
        moveTo(12.0f, 12.0f)
        moveToRelative(-3.2f, 0.0f)
        arcToRelative(3.2f, 3.2f, 0.0f, true, true, 6.4f, 0.0f)
        arcToRelative(3.2f, 3.2f, 0.0f, true, true, -6.4f, 0.0f)
        moveTo(9.0f, 2.0f)
        lineTo(7.17f, 4.0f)
        horizontalLineTo(4.0f)
        curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
        verticalLineToRelative(12.0f)
        curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
        horizontalLineToRelative(16.0f)
        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        verticalLineTo(6.0f)
        curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
        horizontalLineToRelative(-3.17f)
        lineTo(15.0f, 2.0f)
        horizontalLineTo(9.0f)
        close()
    }
}

val Icons.Filled.Psychology: ImageVector get() = Icons.Filled.AutoAwesome

val Icons.Filled.RestartAlt: ImageVector get() = Icons.Filled.History

val Icons.Filled.Save: ImageVector by lazy {
    filledIcon("Filled.Save") {
        moveTo(17.0f, 3.0f)
        horizontalLineTo(5.0f)
        curveToRelative(-1.11f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
        verticalLineToRelative(14.0f)
        curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
        horizontalLineToRelative(14.0f)
        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        verticalLineTo(7.0f)
        lineToRelative(-4.0f, -4.0f)
        close()
        moveTo(12.0f, 19.0f)
        curveToRelative(-1.66f, 0.0f, -3.0f, -1.34f, -3.0f, -3.0f)
        reflectiveCurveToRelative(1.34f, -3.0f, 3.0f, -3.0f)
        reflectiveCurveToRelative(3.0f, 1.34f, 3.0f, 3.0f)
        reflectiveCurveToRelative(-1.34f, 3.0f, -3.0f, 3.0f)
        close()
        moveTo(15.0f, 9.0f)
        horizontalLineTo(5.0f)
        verticalLineTo(5.0f)
        horizontalLineToRelative(10.0f)
        verticalLineToRelative(4.0f)
        close()
    }
}

val Icons.Filled.School: ImageVector by lazy {
    filledIcon("Filled.School") {
        moveTo(5.0f, 13.18f)
        verticalLineToRelative(4.0f)
        lineTo(12.0f, 21.0f)
        lineToRelative(7.0f, -3.82f)
        verticalLineToRelative(-4.0f)
        lineTo(12.0f, 17.0f)
        lineToRelative(-7.0f, -3.82f)
        close()
        moveTo(12.0f, 3.0f)
        lineTo(1.0f, 9.0f)
        lineToRelative(11.0f, 6.0f)
        lineToRelative(9.0f, -4.91f)
        verticalLineTo(17.0f)
        horizontalLineToRelative(2.0f)
        verticalLineTo(9.0f)
        lineTo(12.0f, 3.0f)
        close()
    }
}

val Icons.Filled.Security: ImageVector get() = Icons.Outlined.OutlinedShield

val Icons.Filled.Spa: ImageVector get() = Icons.Filled.AutoAwesome

val Icons.Filled.Stars: ImageVector get() = Icons.Filled.AutoAwesome

val Icons.Filled.Stop: ImageVector by lazy {
    filledIcon("Filled.Stop") {
        moveTo(6.0f, 6.0f)
        horizontalLineToRelative(12.0f)
        verticalLineToRelative(12.0f)
        horizontalLineTo(6.0f)
        close()
    }
}

val Icons.Filled.Verified: ImageVector by lazy {
    filledIcon("Filled.Verified") {
        moveTo(23.0f, 12.0f)
        lineToRelative(-2.44f, -2.79f)
        lineToRelative(0.34f, -3.69f)
        lineToRelative(-3.61f, -0.82f)
        lineToRelative(-1.89f, -3.2f)
        lineTo(12.0f, 2.96f)
        lineTo(8.6f, 1.5f)
        lineTo(6.71f, 4.69f)
        lineTo(3.1f, 5.5f)
        lineToRelative(0.34f, 3.7f)
        lineTo(1.0f, 12.0f)
        lineToRelative(2.44f, 2.79f)
        lineToRelative(-0.34f, 3.7f)
        lineToRelative(3.61f, 0.82f)
        lineToRelative(1.89f, 3.2f)
        lineTo(12.0f, 21.04f)
        lineToRelative(3.4f, 1.46f)
        lineToRelative(1.89f, -3.2f)
        lineToRelative(3.61f, -0.82f)
        lineToRelative(-0.34f, -3.69f)
        lineTo(23.0f, 12.0f)
        close()
        moveTo(10.09f, 16.72f)
        lineToRelative(-3.8f, -3.81f)
        lineToRelative(1.48f, -1.48f)
        lineToRelative(2.32f, 2.33f)
        lineToRelative(5.85f, -5.87f)
        lineToRelative(1.48f, 1.48f)
        lineToRelative(-7.33f, 7.35f)
        close()
    }
}

val Icons.Filled.Videocam: ImageVector by lazy {
    filledIcon("Filled.Videocam") {
        moveTo(17.0f, 10.5f)
        verticalLineTo(7.0f)
        curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
        horizontalLineTo(4.0f)
        curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
        verticalLineToRelative(10.0f)
        curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
        horizontalLineToRelative(12.0f)
        curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
        verticalLineToRelative(-3.5f)
        lineToRelative(4.0f, 4.0f)
        verticalLineToRelative(-11.0f)
        lineToRelative(-4.0f, 4.0f)
        close()
    }
}

val Icons.Filled.WaterDrop: ImageVector by lazy {
    filledIcon("Filled.WaterDrop") {
        moveTo(12.0f, 2.0f)
        curveToRelative(-5.33f, 4.55f, -8.0f, 8.48f, -8.0f, 11.8f)
        curveToRelative(0.0f, 4.98f, 3.8f, 8.2f, 8.0f, 8.2f)
        reflectiveCurveToRelative(8.0f, -3.22f, 8.0f, -8.2f)
        curveToRelative(0.0f, -3.32f, -2.67f, -7.25f, -8.0f, -11.8f)
        close()
    }
}

val Icons.Filled.Work: ImageVector get() = Icons.Filled.Badge

val Icons.Filled.WorkspacePremium: ImageVector get() = Icons.Outlined.OutlinedEmojiEvents
