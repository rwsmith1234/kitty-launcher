package com.rws.kittylauncher.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rws.kittylauncher.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.tv.material3.MaterialTheme

//val LocalLauncherUI = staticCompositionLocalOf { LauncherUIConfig() }

object GlobalConfig {
    var ui by mutableStateOf(LauncherUIConfig())
}

@Serializable
enum class LauncherTextStyle {
    DISPLAY_LARGE,
    DISPLAY_MEDIUM,
    DISPLAY_SMALL,
    HEADLINE_LARGE,
    HEADLINE_MEDIUM,
    HEADLINE_SMALL,
    TITLE_LARGE,
    TITLE_MEDIUM,
    TITLE_SMALL,
    BODY_LARGE,
    BODY_MEDIUM,
    BODY_SMALL,
    LABEL_LARGE,
    LABEL_MEDIUM,
    LABEL_SMALL,
}

@Serializable
data class CategoryCfg(
    val id: String,
    val name: String,
)

@Immutable
@Serializable
data class LauncherUIConfig(
    val appCardInnerBlackBorderWidthDp: Int = 3,
    val appCardBorderColor: Long = 0xFF7B1FA2,
    val appCardMovingBorderColor: Long = 0xFFD50000,
    val appCardBorderWidthDp: Float = 3.0f,
    val appCardMovingBorderWidthDp: Float = 4.0f,
    val appCardFocusedScale: Float = 1.20f,
    val appCardMovingScale: Float = 1.20f, //rws wanted to make even larger if moving but cards in lower rows show on top
    val appCardFocusedGlowAlpha: Float = 0.8f,
    val appCardFocusedGlowElevationDp: Int = 16,
    val appCardContainerMovingAlpha: Float = 0.4f,
    val appCardGraphicsLayerMovingAlpha: Float = 0.5f,
    val appCardTileColor: Long = 0xFF001965,

    val categoryNamesUppercase: Boolean = false,
    val categoryNamesLabelStyle: LauncherTextStyle = LauncherTextStyle.HEADLINE_SMALL,
    val categoryNamesAlpha: Float = 0.6f,
    val categoryNamesLetterSpacingSp: Float = 0.0f,
    val categoryNamesTopPaddingDp: Float = 44.0f,
    val categoryNamesBottomPaddingDp: Float = 0.0f,

    val menuSlidesToCenter: Boolean = false,

    val checkMarkEnabledAlpha: Float = 0.6f,
    val checkMarkDisabledAlpha:Float = .38f,
    val checkMarkEnabledTint:Float = 0.8f,
    val checkMarkDisabledTint:Float = .38f,

    val radioMarkGapDp: Float = 2.0f,

    val statusBarFocusedColor: Long = 0xFF7B1FA2,
)
@Immutable
@Serializable
data class LauncherConfig(
    val ui: LauncherUIConfig = LauncherUIConfig(),
    val categories: List<CategoryCfg> = listOf(
        //************  There are two lists, change both ******************* rws
        CategoryCfg("uncategorized", "Uncategorized"),  //rws uncat
        CategoryCfg("streaming", "Streaming"),
        CategoryCfg("music", "Music"),
        CategoryCfg("utilities", "Utilities"),
        CategoryCfg("games", "Games"),
        CategoryCfg("amazon", "Amazon Apps"),
        CategoryCfg("apps", "Apps"),
    ),
    /** package -> section ids the user assigned (an app may be in several) */
    val sections: Map<String, Set<String>> = emptyMap(),
    /** category id -> explicit package order */
    val order: Map<String, List<String>> = emptyMap(),
    val hidden: Set<String> = emptySet(),
    /** packages seen on the last scan — new installs are auto-added to the first section */
    val knownApps: Set<String> = emptySet(),
    val wallpaper: Int = 6, //Charcoal  //rws pref
    val useCustomWallpaper: Boolean = false,
    /** video wallpaper: persisted content-URI of a local video, looped muted */
    val videoUri: String = "",
    val useVideoWallpaper: Boolean = false,
    /** built-in aerials source */
    val useBuiltinAerials: Boolean = false,
    /** which built-in collection (index into BuiltinAerials.SOURCES; 0 = all) */
    val builtinSource: Int = 0,
    /** video wallpaper playback speed: index into VIDEO_SPEEDS */
    val videoSpeed: Int = 3, // 1x
    val accent: Int = 0,
    val h24: Boolean = false, //rws pref
    val showHidden: Boolean = false,
    val setupDone: Boolean = false,
    // ----- status bar -----
    val showStatusBar: Boolean = true,
    /** package of the app the VPN icon opens; empty = system VPN settings */
    val vpnApp: String = "",
    val showVpnButton: Boolean = true,
    /** wrap the status-bar icons in the same glass panel as dock mode */
    val statusBarGlass: Boolean = true,
    /** index into DATE_FORMATS; 0 = no date shown */
    val dateFormat: Int = 0,
    // ----- display options -----
    /** wallpaper dimming: 0 = top & bottom, 1 = top, 2 = bottom, 3 = full, 4 = off */
    val scrimMode: Int = 1, // Top
    val showCategoryNames: Boolean = true,
    val showAppLabels: Boolean = true,
    /** spacing step 0..4 (see GAP_SIZES) */
    val spacing: Int = 1, // Small
    /** icon size step 0..4 (see ICON_SIZES) */
    val iconScale: Int = 1, // Small
    /** icon/panel corner roundness step 0..4 (see CORNER_RADII) */
    val cornerRadius: Int = 3, // Large
    /** UI scale: 0 = Auto (compact high-DPI TVs), 1..5 = fixed (see UI_SCALES) */
    val uiScale: Int = 0, // Auto
    /** language code (e.g. "fr", "de"); empty = system default */
    val language: String = "",
    /** 0 = carousel (fixed selection), 1 = grid, 2 = dock */
    val layout: Int = 1, //Grid  //rws pref
    val menuAlign: Int = 0, //rws menu
    val columnCount: Int? = 6, //rws col
    val columnGap: Int = 12, //rws col
    val muteNavSounds: Boolean = false,
    val deviceIP: String = "", //rws save deviceIP in backup file
    )

const val LAYOUT_CAROUSEL = 0
const val LAYOUT_GRID = 1
const val LAYOUT_DOCK = 2

/** Status bar date formats (SimpleDateFormat patterns); index 0 = off. */
//rws add new format
val DATE_FORMATS = listOf("", "EEE d", "EEE d MMM", "EEE, MMM d", "d MMM yyyy", "dd/MM", "MM/dd", "yyyy-MM-dd")

/** Sorted by user base — the display name is localized in the UI via string resource. */
val LANGUAGES = listOf(
    "",     // system default
    "zh", "es", "ja", "de", "fr",   // tier 1
    "pt", "ru", "ko", "ar", "it",   // tier 2
    "tr", "pl", "nl", "hi", "th", "in", "vi", // tier 3
)

private val Context.dataStore by preferencesDataStore(name = "launcher")
private val KEY_CONFIG = stringPreferencesKey("config")
private val json = Json { ignoreUnknownKeys = true }

class ConfigStore(private val context: Context) {

    val flow: Flow<LauncherConfig> = context.dataStore.data.map { prefs ->
        prefs[KEY_CONFIG]?.let {
            runCatching { json.decodeFromString<LauncherConfig>(it) }.getOrNull()
        } ?: LauncherConfig(categories = defaultCategories(context))
    }

    private fun defaultCategories(ctx: Context) = listOf(
        //************  There are two lists, change both ******************* rws
        CategoryCfg("uncategorized", "Uncategorized"), //rws uncat
        CategoryCfg("streaming", ctx.getString(R.string.cat_streaming)),
        CategoryCfg("music", ctx.getString(R.string.cat_music)),
        CategoryCfg("utilities", "Utilities"),
        CategoryCfg("games", ctx.getString(R.string.cat_games)),
        CategoryCfg("amazon", "Amazon Apps"),
        CategoryCfg("apps", ctx.getString(R.string.cat_apps)),
    )

    suspend fun update(transform: (LauncherConfig) -> LauncherConfig) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_CONFIG]?.let {
                runCatching { json.decodeFromString<LauncherConfig>(it) }.getOrNull()
            } ?: LauncherConfig()
            prefs[KEY_CONFIG] = json.encodeToString(transform(current))
        }
    }
}

@Composable
fun launcherTextStyle(style: LauncherTextStyle): TextStyle {
    return when (style) {
        LauncherTextStyle.DISPLAY_LARGE -> MaterialTheme.typography.displayLarge
        LauncherTextStyle.DISPLAY_MEDIUM -> MaterialTheme.typography.displayMedium
        LauncherTextStyle.DISPLAY_SMALL -> MaterialTheme.typography.displaySmall
        LauncherTextStyle.HEADLINE_LARGE -> MaterialTheme.typography.headlineLarge
        LauncherTextStyle.HEADLINE_MEDIUM -> MaterialTheme.typography.headlineMedium
        LauncherTextStyle.HEADLINE_SMALL -> MaterialTheme.typography.headlineSmall
        LauncherTextStyle.TITLE_LARGE -> MaterialTheme.typography.titleLarge
        LauncherTextStyle.TITLE_MEDIUM -> MaterialTheme.typography.titleMedium
        LauncherTextStyle.TITLE_SMALL -> MaterialTheme.typography.titleSmall
        LauncherTextStyle.BODY_LARGE -> MaterialTheme.typography.bodyLarge
        LauncherTextStyle.BODY_MEDIUM -> MaterialTheme.typography.bodyMedium
        LauncherTextStyle.BODY_SMALL -> MaterialTheme.typography.bodySmall
        LauncherTextStyle.LABEL_LARGE -> MaterialTheme.typography.labelLarge
        LauncherTextStyle.LABEL_MEDIUM -> MaterialTheme.typography.labelMedium
        LauncherTextStyle.LABEL_SMALL -> MaterialTheme.typography.labelSmall
    }
}
