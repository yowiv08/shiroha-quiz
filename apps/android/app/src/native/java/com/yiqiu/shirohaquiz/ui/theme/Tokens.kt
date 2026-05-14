package com.yiqiu.shirohaquiz.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object ShirohaColors {
    var isDarkMode: Boolean = false

    val BgApp: Color
        get() = if (isDarkMode) Color(0xFF0E1627) else Color(0xFFF4F6FB)
    val BgElevated: Color
        get() = if (isDarkMode) Color(0xFF13203A) else Color(0xFFFBFCFE)
    val BgDeepFocus = Color(0xFF0E1627)

    val BgGradientTop: Color
        get() = if (isDarkMode) Color(0xFF0E1627) else Color(0xFFF7F9FF)
    val BgGradientMiddle: Color
        get() = if (isDarkMode) Color(0xFF101B31) else Color(0xFFF1F4FB)
    val BgGradientBottom: Color
        get() = if (isDarkMode) Color(0xFF15213A) else Color(0xFFF6F7FB)

    val CardGlass: Color
        get() = if (isDarkMode) Color(0xD91A2742) else Color(0xC7FFFFFF)
    val CardSoft: Color
        get() = if (isDarkMode) Color(0xE617233D) else Color(0xE6FFFFFF)
    val CardMuted: Color
        get() = if (isDarkMode) Color(0xFF1B2844) else Color(0xFFF7F9FD)
    val CardWhite84: Color
        get() = if (isDarkMode) Color(0xD61A2742) else Color.White.copy(alpha = 0.84f)
    val CardWhite86: Color
        get() = if (isDarkMode) Color(0xDB1B2945) else Color.White.copy(alpha = 0.86f)
    val CardWhite78: Color
        get() = if (isDarkMode) Color(0xC71A2742) else Color.White.copy(alpha = 0.78f)
    val CardWhite72: Color
        get() = if (isDarkMode) Color(0xB817233D) else Color.White.copy(alpha = 0.72f)
    val CardWhite68: Color
        get() = if (isDarkMode) Color(0xAD17233D) else Color.White.copy(alpha = 0.68f)
    val CardWhite62: Color
        get() = if (isDarkMode) Color(0x9E17233D) else Color.White.copy(alpha = 0.62f)
    val BottomBar: Color
        get() = if (isDarkMode) Color(0xF0121D33) else Color.White.copy(alpha = 0.82f)

    val LineSoft: Color
        get() = if (isDarkMode) Color(0x40B7C8E8) else Color(0x38A0AEC0)
    val LineStrong: Color
        get() = if (isDarkMode) Color(0xFF334462) else Color(0xFFD8E0EF)
    val LineSelected: Color
        get() = if (isDarkMode) Color(0xFF91AFFF) else Color(0xFF85A7FF)

    val TextPrimary: Color
        get() = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF101828)
    val TextSecondary: Color
        get() = if (isDarkMode) Color(0xFFB8C2D6) else Color(0xFF667085)
    val TextTertiary: Color
        get() = if (isDarkMode) Color(0xFF8794AA) else Color(0xFF94A3B8)
    val TextOnBrand: Color
        get() = if (isDarkMode) Color(0xFF0F172A) else Color.White
    val TextWarning: Color
        get() = if (isDarkMode) Color(0xFFF9C46B) else Color(0xFF9A6700)
    val IconWarning: Color
        get() = if (isDarkMode) Color(0xFFF4B348) else Color(0xFFE29A00)

    val BrandPrimary: Color
        get() = if (isDarkMode) Color(0xFF89A7FF) else Color(0xFF4F7CFF)
    val BrandPrimarySoft: Color
        get() = if (isDarkMode) Color(0xFF243A63) else Color(0xFFEAF0FF)
    val BrandSecondary: Color
        get() = if (isDarkMode) Color(0xFFB7C8FF) else Color(0xFF6C8EEA)

    val OptionLabelIdle: Color
        get() = if (isDarkMode) Color(0xFF25324F) else Color(0xFFF3F5FA)

    val StateSuccess: Color
        get() = if (isDarkMode) Color(0xFF5FE3A2) else Color(0xFF17B26A)
    val StateSuccessSoft: Color
        get() = if (isDarkMode) Color(0xFF123A2A) else Color(0xFFDDF7EA)
    val StateWarning: Color
        get() = if (isDarkMode) Color(0xFFF9B955) else Color(0xFFF79009)
    val StateWarningSoft: Color
        get() = if (isDarkMode) Color(0xFF3B2B12) else Color(0xFFFFF7E8)
    val StateDanger: Color
        get() = if (isDarkMode) Color(0xFFFF8A80) else Color(0xFFF04438)
    val StateDangerSoft: Color
        get() = if (isDarkMode) Color(0xFF3B1E24) else Color(0xFFFEECEC)
}

object ShirohaRadius {
    val Sm = 14.dp
    val Md = 18.dp
    val Lg = 24.dp
    val Xl = 30.dp
    val Pill = 999.dp
}

object ShirohaSpacing {
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 12.dp
    val Lg = 16.dp
    val Xl = 20.dp
    val Xxl = 24.dp
    val Xxxl = 32.dp
}

object ShirohaDimens {
    val Hairline = 1.dp
    val PageHorizontalPadding = ShirohaSpacing.Xl
    val PageVerticalPadding = ShirohaSpacing.Sm

    val HeroCardHeight = 132.dp
    val HeroImageSize = 92.dp
    val HeroImageFrameSize = 100.dp
    val HeroImageFrameExtra = 8.dp
    val HeroImageAlpha = 0.92f

    val StepPillWidth = 136.dp
    val StepPillMinHeight = 28.dp
    val StepPillHorizontalPadding = 10.dp
    val StepPillVerticalPadding = 5.dp

    val StatusChipMinHeight = 32.dp
    val StatusChipHorizontalPadding = 12.dp
    val StatusChipVerticalPadding = 7.dp

    val ActionButtonMinHeight = 44.dp
    val ActionButtonIconSize = 17.dp
    val ActionButtonHorizontalPadding = 14.dp
    val ActionButtonEqualHorizontalPadding = 10.dp
    val ActionButtonIconTextGap = 7.dp
    val ActionButtonEqualIconTextGap = 6.dp
    val DisabledAlpha = 0.52f

    val OptionCardHorizontalPadding = 14.dp
    val OptionCardVerticalPadding = 14.dp
    val OptionLabelSize = 36.dp
    val OptionLabelTextGap = 12.dp

    val BottomBarHorizontalPadding = 12.dp
    val BottomBarVerticalPadding = 4.dp
    val BottomNavItemGap = 6.dp
    val BottomNavItemHeight = 56.dp
    val BottomNavItemHorizontalPadding = 4.dp
    val BottomNavIconSize = 21.dp
    val BottomNavIconSelectedScale = 1.05f

    val EmptyStateImageSize = 140.dp
    val LoadingImageSize = 112.dp
}

object ShirohaMotion {
    const val PageTransitionMillis = 180
    const val PageFadeOutMillis = 90
    const val BottomNavMillis = 140
    const val HeroFloatMillis = 1700
    const val LoadingScaleMillis = 900
    const val SplashFadeMillis = 420
    const val SplashHoldMillis = 1450

    const val PageTransitionOffsetPx = 6
    val HeroFloatDistance = 2.6.dp
    const val LoadingScaleMin = 0.98f
    const val LoadingScaleMax = 1.02f
}
