package com.rws.kittylauncher.ui

import android.annotation.SuppressLint
import android.content.Context
import android.database.ContentObserver
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.rws.kittylauncher.R

// ──────────────────────────────────────────────────────────────
// CompositionLocals
// ──────────────────────────────────────────────────────────────
val LocalMuteNavSounds = compositionLocalOf { false }
val LocalSystemNavSoundsEnabled = compositionLocalOf { true }

// ──────────────────────────────────────────────────────────────
// System Sound Reader & Observer
// ──────────────────────────────────────────────────────────────

/**
 * Reads the current system sound effect setting directly from ContentResolver.
 */
fun readSystemSoundSetting(context: Context): Boolean {
    val cr = context.contentResolver
    return Settings.System.getString(cr, Settings.System.SOUND_EFFECTS_ENABLED)?.let { it != "0" }
        ?: Settings.Global.getString(cr, Settings.System.SOUND_EFFECTS_ENABLED)?.let { it != "0" }
        ?: Settings.Secure.getString(cr, Settings.System.SOUND_EFFECTS_ENABLED)?.let { it != "0" }
        ?: true
}

/**
 * Observes system sound setting changes in real-time using a ContentObserver.
 * Triggers recomposition whenever system sound settings are toggled.
 */
@Composable
fun rememberSystemNavSoundsEnabled(context: Context = LocalContext.current): Boolean {
    var isEnabled by remember(context) {
        mutableStateOf(readSystemSoundSetting(context))
    }

    DisposableEffect(context) {
        val cr = context.contentResolver
        val handler = Handler(Looper.getMainLooper())
        val observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                isEnabled = readSystemSoundSetting(context)
            }
        }

        val systemUri = Settings.System.getUriFor(Settings.System.SOUND_EFFECTS_ENABLED)
        val globalUri = Settings.Global.getUriFor(Settings.System.SOUND_EFFECTS_ENABLED)
        val secureUri = Settings.Secure.getUriFor(Settings.System.SOUND_EFFECTS_ENABLED)

        if (systemUri != null) cr.registerContentObserver(systemUri, false, observer)
        if (globalUri != null && globalUri != systemUri) cr.registerContentObserver(globalUri, false, observer)
        if (secureUri != null && secureUri != systemUri && secureUri != globalUri) cr.registerContentObserver(secureUri, false, observer)

        onDispose {
            cr.unregisterContentObserver(observer)
        }
    }

    return isEnabled
}

// ──────────────────────────────────────────────────────────────
// Single combined check
// ──────────────────────────────────────────────────────────────

/**
 * True only if:
 *   • App-level mute is OFF
 *   • View-level sound effects enabled
 *   • System SOUND_EFFECTS_ENABLED is ON
 *   • System stream not muted & volume > 0 (live)
 */
fun shouldPlayNavSound(view: View, appMuted: Boolean): Boolean {
    if (appMuted) return false
    if (!view.isSoundEffectsEnabled) return false

    val context = view.context
    if (!readSystemSoundSetting(context)) return false

    val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    return am?.let {
        val volumeZero = it.getStreamVolume(AudioManager.STREAM_SYSTEM) == 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            !it.isStreamMute(AudioManager.STREAM_SYSTEM) && !volumeZero
        } else {
            !volumeZero
        }
    } ?: false
}


// ──────────────────────────────────────────────────────────────
// Sound players
// ──────────────────────────────────────────────────────────────
fun playBackSound(view: View, isMuted: Boolean) {
    if (shouldPlayNavSound(view, isMuted)) BackSoundPlayer.play(view.context)
}

fun playNavSound(view: View, isMuted: Boolean) {
    if (!isMuted && view.isSoundEffectsEnabled) {
        view.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN)
    }
}

fun playClickSound(view: View, isMuted: Boolean) {
    if (!isMuted && view.isSoundEffectsEnabled) {
        view.playSoundEffect(SoundEffectConstants.CLICK)
    }
}

// ──────────────────────────────────────────────────────────────
// Modifiers
// ──────────────────────────────────────────────────────────────
@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.verticalNavSound(): Modifier = composed {
    val view = LocalView.current
    val appMuted = LocalMuteNavSounds.current
    onPreviewKeyEvent { e ->
        if (e.type == KeyEventType.KeyDown) {
            when (e.key) {
                Key.DirectionUp, Key.DirectionDown -> {
                    if (shouldPlayNavSound(view, appMuted)) {
                        view.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN)
                    }
                    false
                }
                else -> false
            }
        } else false
    }
}

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.horizontalNavSound(): Modifier = composed {
    val view = LocalView.current
    val appMuted = LocalMuteNavSounds.current
    onPreviewKeyEvent { e ->
        if (e.type == KeyEventType.KeyDown) {
            when (e.key) {
                Key.DirectionRight, Key.DirectionLeft -> {
                    if (shouldPlayNavSound(view, appMuted)) {
                        view.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN)
                    }
                    false
                }
                else -> false
            }
        } else false
    }
}

// ──────────────────────────────────────────────────────────────
// BackSoundPlayer (unchanged)
// ──────────────────────────────────────────────────────────────
private object BackSoundPlayer {
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var isLoaded: Boolean = false

    fun play(context: Context) {
        val appContext = context.applicationContext
        if (soundPool == null) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build()

            pool.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) {
                    isLoaded = true
                    pool.play(soundId, 1f, 1f, 1, 0, 1f)
                }
            }

            soundId = pool.load(appContext, R.raw.keypress_delete, 1)
            soundPool = pool
        } else if (isLoaded) {
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }
}