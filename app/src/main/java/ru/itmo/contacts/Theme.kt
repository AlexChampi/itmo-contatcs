package ru.itmo.contacts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object OneUiColors {
    val Background = Color(0xFFF6F6F6)
    val Surface = Color(0xFFFAFAFA)
    val Ink = Color(0xFF111111)
    val InkMuted = Color(0xFF6E6E6E)
    val Divider = Color(0xFFE4E4E4)
    val Pill = Color(0xFFE4E4E4)
    val Call = Color(0xFF17B26A)
    val Avatars = listOf(
        Color(0xFFF29A90),
        Color(0xFF7FC7F5),
        Color(0xFFF58CB5),
        Color(0xFFFDA07A),
        Color(0xFFB5A4F2),
        Color(0xFF8496F0),
    )
}

object OneUiType {
    val TitleLarge = TextStyle(fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Normal)
    val Subtitle = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal)
    val HeaderSmall = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold)
    val ListPrimary = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal)
    val SectionIndex = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
    val Button = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold)
    val AvatarInitial = TextStyle(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal)
}

object OneUiDimens {
    val ScreenPadding = 16.dp
    val CardSpacing = 24.dp
    val CardRadius = 22.dp
    val RowHeight = 64.dp
    val AvatarSize = 44.dp
    val HeaderHeight = 56.dp
}

@Composable
fun OneUiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = OneUiColors.Call,
            background = OneUiColors.Background,
            surface = OneUiColors.Surface,
            onBackground = OneUiColors.Ink,
            onSurface = OneUiColors.Ink,
        ),
        content = content,
    )
}
