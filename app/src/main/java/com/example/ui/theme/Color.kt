package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val cyberBackground: Color,
    val cyberSurface: Color,
    val cyberSurfaceVariant: Color,
    val cyberCardBorder: Color,
    val electricCyan: Color,
    val electricCyanDark: Color,
    val neonEmerald: Color,
    val neonAmber: Color,
    val neonCrimson: Color,
    val terminalGreen: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val slotActiveBadge: Color,
    val slotInactiveBadge: Color
)

val DarkAppColors = AppColors(
    cyberBackground = Color(0xFF090D16),
    cyberSurface = Color(0xFF111726),
    cyberSurfaceVariant = Color(0xFF192237),
    cyberCardBorder = Color(0xFF263352),
    electricCyan = Color(0xFF00E5FF),
    electricCyanDark = Color(0xFF00B0FF),
    neonEmerald = Color(0xFF00E676),
    neonAmber = Color(0xFFFFB300),
    neonCrimson = Color(0xFFFF3D71),
    terminalGreen = Color(0xFF10B981),
    textPrimary = Color(0xFFF1F5F9),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    slotActiveBadge = Color(0xFF00E5FF),
    slotInactiveBadge = Color(0xFF475569)
)

val LightAppColors = AppColors(
    cyberBackground = Color(0xFFF8FAFC),
    cyberSurface = Color(0xFFFFFFFF),
    cyberSurfaceVariant = Color(0xFFF1F5F9),
    cyberCardBorder = Color(0xFFE2E8F0),
    electricCyan = Color(0xFF0284C7), // High-contrast readable cyan/blue for light theme
    electricCyanDark = Color(0xFF0369A1),
    neonEmerald = Color(0xFF16A34A),
    neonAmber = Color(0xFFD97706),
    neonCrimson = Color(0xFFDC2626),
    terminalGreen = Color(0xFF059669),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF64748B),
    slotActiveBadge = Color(0xFF0284C7),
    slotInactiveBadge = Color(0xFF94A3B8)
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

val CyberBackground: Color @Composable get() = LocalAppColors.current.cyberBackground
val CyberSurface: Color @Composable get() = LocalAppColors.current.cyberSurface
val CyberSurfaceVariant: Color @Composable get() = LocalAppColors.current.cyberSurfaceVariant
val CyberCardBorder: Color @Composable get() = LocalAppColors.current.cyberCardBorder
val ElectricCyan: Color @Composable get() = LocalAppColors.current.electricCyan
val ElectricCyanDark: Color @Composable get() = LocalAppColors.current.electricCyanDark
val NeonEmerald: Color @Composable get() = LocalAppColors.current.neonEmerald
val NeonAmber: Color @Composable get() = LocalAppColors.current.neonAmber
val NeonCrimson: Color @Composable get() = LocalAppColors.current.neonCrimson
val TerminalGreen: Color @Composable get() = LocalAppColors.current.terminalGreen
val TextPrimary: Color @Composable get() = LocalAppColors.current.textPrimary
val TextSecondary: Color @Composable get() = LocalAppColors.current.textSecondary
val TextMuted: Color @Composable get() = LocalAppColors.current.textMuted
val SlotActiveBadge: Color @Composable get() = LocalAppColors.current.slotActiveBadge
val SlotInactiveBadge: Color @Composable get() = LocalAppColors.current.slotInactiveBadge
