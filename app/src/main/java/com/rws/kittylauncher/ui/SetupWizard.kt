package com.rws.kittylauncher.ui

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.rws.kittylauncher.Actions
import com.rws.kittylauncher.R
import java.net.Inet4Address
import java.net.NetworkInterface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.drawscope.clipRect
import android.util.Log
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.withTransform
import java.util.Locale
import androidx.compose.ui.platform.LocalConfiguration

/**
 * First-launch wizard. Detects the device type and guides the user to set
 * Kitty as the default home — including certified Google TV devices, where
 * the system UI does not allow changing the home app and ADB is required.
 * [onVpnChosen] receives the picked VPN package, or null to hide the VPN icon.
 */
@Composable
fun SetupWizard(onDone: () -> Unit, onVpnChosen: (String?) -> Unit) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(0) }
    var gtvPage by remember { mutableIntStateOf(0) } // carousel page on the Google-TV step
    var defaultHome by remember { mutableStateOf(defaultHomePackage(context)) }
    val isDefault = defaultHome == context.packageName
    val isGoogleTv = remember {
        // "Amati" feature = certified Google TV experience (home app locked by Google)
        context.packageManager.hasSystemFeature("com.google.android.feature.AMATI_EXPERIENCE")
    }
    val ip = remember { deviceIp() }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { defaultHome = defaultHomePackage(context) }

    fun requestDefault() {
        if (Build.VERSION.SDK_INT >= 29) {
            val rm = context.getSystemService(RoleManager::class.java)
            if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !rm.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                val ok = runCatching {
                    roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_HOME))
                }.isSuccess
                if (ok) return
            }
        }
        runCatching { context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) }
            .onFailure {
                Actions.toast(context, context.getString(R.string.wizard_no_chooser))
            }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(WALLPAPERS[5].brush()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .width(680.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 36.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Branding: the Kitty, on every wizard step.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    //rws
                    //painter = painterResource(R.drawable.ic_Kitty),
                    painter = painterResource(R.drawable.ic_kitty),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                )
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                )
                //rws
                Text(
                    stringResource(R.string.mistakes_by),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
            when (step) {
                0 -> {
                    //rws
                    //Title(stringResource(R.string.wizard_welcome_title))
                    AnimatedWelcomeTitle()
                    Body(stringResource(R.string.wizard_welcome_body))
                    Body(stringResource(R.string.wizard_privacy_body))
                    NavRow(nextLabel = stringResource(R.string.next)) { step = 1 }
                }
                1 -> {
                    Title(stringResource(R.string.wizard_home_title))
                    if (isDefault) {
                        Body(stringResource(R.string.wizard_already_default))
                        NavRow(
                            backAction = { step = 0 },
                            nextLabel = stringResource(R.string.next),
                        ) { step = 2 }
                    } else if (isGoogleTv) {
                        // Carousel: 3 short pages so it fits & stays centered at
                        // any DPI. Prev/Next moves between pages; on the ends it
                        // steps out of the wizard.
                        when (gtvPage) {
                            0 -> {
                                Body(stringResource(R.string.wizard_gtv_intro))
                                Body(stringResource(R.string.wizard_gtv_adb))
                                CodeLine("adb connect ${ip ?: "<TV-IP>"}:5555")
                                CodeLine("adb shell cmd package set-home-activity com.rws.kittylauncher/.MainActivity")
                            }
                            1 -> {
                                Body(stringResource(R.string.wizard_gtv_overlay))
                                CodeLine("adb shell cmd package resolve-activity -a android.intent.action.MAIN -c android.intent.category.HOME")
                                CodeLine("adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx")
                                CodeLine("adb shell pm disable-user --user 0 com.google.android.tvlauncher")
                                CodeLine("adb shell pm disable-user --user 0 com.google.android.tungsten.setupwraith")
                            }
                            else -> {
                                Body(stringResource(R.string.wizard_gtv_which))
                                Body(stringResource(R.string.wizard_gtv_remap))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(onClick = { requestDefault() }) {
                                        Text(stringResource(R.string.wizard_try_dialog))
                                    }
                                    Button(onClick = { defaultHome = defaultHomePackage(context) }) {
                                        Text(stringResource(R.string.wizard_check_again))
                                    }
                                }
                            }
                        }
                        PagerRow(
                            page = gtvPage,
                            count = 3,
                            onPrev = { if (gtvPage > 0) gtvPage-- else step = 0 },
                            onNext = { if (gtvPage < 2) gtvPage++ else step = 2 },
                        )
                    } else {
                        Body(stringResource(R.string.wizard_aosp_body))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = { requestDefault() }) {
                                Text(stringResource(R.string.wizard_set_default))
                            }
                            Button(onClick = { defaultHome = defaultHomePackage(context) }) {
                                Text(stringResource(R.string.wizard_check_again))
                            }
                        }
                        Body(stringResource(R.string.wizard_no_dialog_hint))
                        NavRow(
                            backAction = { step = 0 },
                            nextLabel = stringResource(R.string.wizard_skip),
                        ) { step = 2 }
                    }
                }
                2 -> {
                    Title(stringResource(R.string.wizard_vpn_title))
                    Body(stringResource(R.string.wizard_vpn_body))
                    val vpns = remember { vpnApps(context) }
                    if (vpns.isEmpty()) {
                        Body(stringResource(R.string.wizard_vpn_none))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            vpns.forEach { (pkg, label) ->
                                Button(onClick = { onVpnChosen(pkg); step = 3 }) { Text(label) }
                            }
                        }
                    }
                    NavRow(
                        backAction = { step = 1 },
                        nextLabel = stringResource(R.string.wizard_vpn_skip),
                    ) { onVpnChosen(null); step = 3 }
                }
                else -> {
                    Title(stringResource(R.string.wizard_done_title))
                    Body(
                        stringResource(R.string.wizard_tips) +
                            if (!isDefault) "\n\n" + stringResource(R.string.wizard_rerun_note) else ""
                    )
                    NavRow(
                        backAction = { step = 2 },
                        nextLabel = stringResource(R.string.wizard_start),
                    ) { onDone() }
                }
            }
        }
    }
}

/* ------------------------------ pieces ------------------------------ */

@Composable
private fun Title(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, color = Color.White)
}

@Composable
private fun Body(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.85f),
    )
}

@Composable
private fun CodeLine(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF8AB4F8),
        )
    }
}

@Composable
private fun NavRow(
    backAction: (() -> Unit)? = null,
    nextLabel: String,
    nextAction: () -> Unit,
) {
    // Focus the primary (next) button on entry so every step opens with a
    // button selected and the D-pad ready.
    val nextFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(80)
        runCatching { nextFocus.requestFocus() }
    }
    Row(
        Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (backAction != null) {
            Button(onClick = backAction) { Text(stringResource(R.string.back)) }
        }
        Button(onClick = nextAction, modifier = Modifier.focusRequester(nextFocus)) {
            Text(nextLabel)
        }
    }
}

/** Carousel controls: Prev / "n / N" / Next. On the ends they step out. */
@Composable
private fun PagerRow(page: Int, count: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    val nextFocus = remember { FocusRequester() }
    LaunchedEffect(page) {
        delay(80)
        runCatching { nextFocus.requestFocus() }
    }
    Row(
        Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(onClick = onPrev) {
            Text(if (page > 0) "◄" else stringResource(R.string.back))
        }
        Text(
            "${page + 1} / $count",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
        )
        Button(onClick = onNext, modifier = Modifier.focusRequester(nextFocus)) {
            Text(if (page < count - 1) "►" else stringResource(R.string.wizard_skip))
        }
    }
}

/* ------------------------------ helpers ------------------------------ */

fun defaultHomePackage(context: Context): String? {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    return context.packageManager
        .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        ?.activityInfo?.packageName
}

private fun deviceIp(): String? = runCatching {
    NetworkInterface.getNetworkInterfaces().toList()
        .flatMap { it.inetAddresses.toList() }
        .firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
        ?.hostAddress
}.getOrNull()

/** Installed VPN apps = anything implementing android.net.VpnService. */
private fun vpnApps(context: Context): List<Pair<String, String>> {
    val pm = context.packageManager
    return pm.queryIntentServices(Intent("android.net.VpnService"), 0)
        .mapNotNull { ri ->
            val si = ri.serviceInfo ?: return@mapNotNull null
            if (si.packageName == context.packageName) return@mapNotNull null
            val label = runCatching { ri.loadLabel(pm).toString() }.getOrNull() ?: si.packageName
            si.packageName to label
        }
        .distinctBy { it.first }
        .sortedBy { it.second.lowercase() }
}

@Composable
private fun AnimatedWelcomeTitle() {
    val initialDelayMillis = 1000
    val scratchDurationMillis = 400
    val replacementDelayMillis = 1000
    val replacementDurationMillis = 1000
    val localizedDelayMillis = 1000
    val localizedDurationMillis = 1500

    // Extra scratch width past the right edge of the original name.
    val scratchExtraRightDp = 24

    val isEnglish = LocalConfiguration.current.locales[0].language == Locale.ENGLISH.language

    LaunchedEffect(isEnglish) {
        Log.d("KittyLauncher", "AnimatedWelcomeTitle: isEnglish=$isEnglish")
    }

    val strikeProgress = remember { Animatable(0f) }
    val replacementProgress = remember { Animatable(0f) }
    val localizedProgress = remember { Animatable(0f) }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val density = LocalDensity.current
    val clawPainter = painterResource(R.drawable.claw_marks)

    val titleBase = stringResource(R.string.wizard_welcome_title_base)
    val originalName = stringResource(R.string.wizard_welcome_title_orig_name)
    val newName = stringResource(R.string.wizard_welcome_title_new_name)

    LaunchedEffect(Unit) {
        delay(initialDelayMillis.toLong())

        strikeProgress.animateTo(
            1f,
            animationSpec = tween(
                durationMillis = scratchDurationMillis,
                easing = FastOutSlowInEasing,
            ),
        )

        delay(replacementDelayMillis.toLong())

        replacementProgress.animateTo(
            1f,
            animationSpec = tween(
                durationMillis = replacementDurationMillis,
                easing = FastOutSlowInEasing,
            ),
        )

        if (!isEnglish) {
            delay(localizedDelayMillis.toLong())

            localizedProgress.animateTo(
                1f,
                animationSpec = tween(
                    durationMillis = localizedDurationMillis,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }

    val originalNameStartIndex = titleBase.length

    val title = buildAnnotatedString {
        append(titleBase)
        append(originalName)
    }

    val layout = textLayoutResult

    val boxModifier = if (layout != null) {
        Modifier.width(
            with(density) {
                layout.size.width.toDp() + scratchExtraRightDp.dp
            }
        )
    } else {
        Modifier
    }

    Box(boxModifier) {

        // This Box owns the old text and scratch only.
        // The replacement text below is outside its clipping area.
        Box(
            Modifier
                .fillMaxWidth()
                .drawWithContent {
                    val result = layout

                    if (result == null) {
                        this@drawWithContent.drawContent()
                        return@drawWithContent
                    }

                    val start = result.getBoundingBox(originalNameStartIndex)
                    val end = result.getBoundingBox(
                        originalNameStartIndex + originalName.length - 1
                    )

                    val originalNameWidth = end.right - start.left
                    val originalNameHeight = end.bottom - start.top

                    val scratchExtraRight =
                        with(density) { scratchExtraRightDp.dp.toPx() }

                    val scratchWidth =
                        originalNameWidth + scratchExtraRight

                    val gap = with(density) { 16.dp.toPx() }

                    val coverLeft =
                        start.left +
                                originalNameWidth +
                                gap -
                                (originalNameWidth + gap) *
                                replacementProgress.value

                    clipRect(
                        right = if (replacementProgress.value > 0f) {
                            coverLeft
                        } else {
                            size.width
                        }
                    ) {
                        this@drawWithContent.drawContent()

                        if (strikeProgress.value > 0f &&
                            replacementProgress.value < 1f
                        ) {
                            clipRect(
                                left = start.left,
                                right = start.left +
                                        scratchWidth *
                                        strikeProgress.value,
                                top = start.top,
                                bottom = start.top + originalNameHeight,
                            ) {
                                withTransform({
                                    translate(
                                        left = start.left,
                                        top = start.top,
                                    )
                                }) {
                                    with(clawPainter) {
                                        draw(
                                            size = Size(
                                                scratchWidth,
                                                originalNameHeight,
                                            ),
                                            alpha = 1f - localizedProgress.value,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                onTextLayout = { textLayoutResult = it },
                modifier = Modifier.graphicsLayer {
                    alpha = 1f - localizedProgress.value
                },
            )
        }

        // Replacement text is outside the scratch/erase clipping Box.
        if (replacementProgress.value > 0f) {
            val start = layout?.getBoundingBox(originalNameStartIndex)
            val end = layout?.getBoundingBox(
                originalNameStartIndex + originalName.length - 1
            )

            if (start != null && end != null) {
                val originalNameWidth = end.right - start.left
                val gap = with(density) { 16.dp.toPx() }

                Text(
                    text = newName,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.graphicsLayer {
                        alpha =
                            replacementProgress.value *
                                    (1f - localizedProgress.value)

                        translationX =
                            start.left +
                                    originalNameWidth +
                                    gap -
                                    (originalNameWidth + gap) *
                                    replacementProgress.value
                    },
                )
            }
        }

        if (!isEnglish) {
            val localizedTitle = stringResource(R.string.wizard_welcome_title)

            var localizedTitleWidth by remember { mutableStateOf(0) }
            var spaceWidth by remember { mutableStateOf(0) }

            val localizedExtraSpaces = if (
                localizedTitleWidth > 0 &&
                spaceWidth > 0 &&
                layout != null
            ) {
                val targetWidth = layout.size.width + spaceWidth
                val missingWidth =
                    (targetWidth - localizedTitleWidth).coerceAtLeast(0)

                (missingWidth + spaceWidth - 1) / spaceWidth
            } else {
                0
            }

            val localizedTitleWithPadding =
                localizedTitle + " ".repeat(localizedExtraSpaces)

            Text(
                text = localizedTitleWithPadding,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                onTextLayout = { result ->
                    localizedTitleWidth = result.size.width
                },
                modifier = Modifier.graphicsLayer {
                    alpha = localizedProgress.value
                },
            )

            // Measure one actual space using the same text style.
            Text(
                text = " ",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Transparent,
                onTextLayout = { result ->
                    spaceWidth = result.size.width
                },
            )
        }
    }
}
