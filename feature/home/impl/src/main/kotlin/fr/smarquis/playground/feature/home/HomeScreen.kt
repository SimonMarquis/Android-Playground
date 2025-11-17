package fr.smarquis.playground.feature.home

import android.util.DisplayMetrics.DENSITY_XXXHIGH
import androidx.activity.compose.ReportDrawn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Horizontal
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Top
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.LongPress
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily.Companion.Monospace
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Light
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import fr.smarquis.playground.core.di.DisplayMetrics
import fr.smarquis.playground.core.ui.PlaygroundTheme
import fr.smarquis.playground.core.ui.asAnnotatedString
import fr.smarquis.playground.core.utils.navigation.BackStack
import fr.smarquis.playground.domain.dice.Dice
import fr.smarquis.playground.domain.settings.Settings
import fr.smarquis.playground.feature.fr.smarquis.playground.feature.licenses.Licenses
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDateTime
import kotlin.text.Typography.times

@Composable
internal fun HomeScreen(
    backStack: BackStack,
) = HomeScreen(
    navigateToLicenses = { backStack.add(Licenses) },
)

@Composable
private fun HomeScreen(
    navigateToLicenses: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val rolls: ImmutableList<Dice> by viewModel.rolls.collectAsState()
    val settings: Settings by viewModel.settings.collectAsState()
    HomeScreenContent(
        navigateToLicenses = navigateToLicenses,
        modifier = modifier,
        rolls = rolls,
        settings = settings,
        data = viewModel.data,
        roll = viewModel::roll,
        reset = viewModel::reset,
        update = viewModel::update,
    )
    ReportDrawn()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    navigateToLicenses: () -> Unit,
    rolls: ImmutableList<Dice>,
    settings: Settings,
    data: HomeData,
    roll: () -> Unit,
    reset: () -> Unit,
    update: (Settings) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier
            .testTag("home")
            .fillMaxSize()
            .background(PlaygroundTheme.colorScheme.background)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.playground_feature_home_title),
                        style = PlaygroundTheme.typography.displaySmall,
                        fontWeight = FontWeight.Thin,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                windowInsets = WindowInsets.safeDrawing.only(Horizontal + Top),
                scrollBehavior = scrollBehavior,
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.playground_feature_home_oss_licenses)) },
                                onClick = { expanded = false; navigateToLicenses() },
                            )
                        }
                    }
                },
            )
        },
        containerColor = Color.Transparent,
        contentColor = contentColorFor(PlaygroundTheme.colorScheme.background),
        contentWindowInsets = WindowInsets.safeDrawing.only(Horizontal),
    ) { contentPadding ->
        val haptic = LocalHapticFeedback.current
        LazyColumn(
            contentPadding = contentPadding,
            modifier = Modifier.testTag("home::list"),
        ) {
            category(key = "tools", title = "Tools", icon = Icons.Default.Settings)
            entry(
                key = "dice",
                title = buildAnnotatedString {
                    append("🎲 Dice ")
                    withStyle(style = SpanStyle(fontStyle = Italic, fontWeight = Light)) {
                        append("(tap to roll new dice, long-press to reset)")
                    }
                },
                message = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 20.sp)) {
                        if (rolls.isEmpty()) append("⚀⚁⚂⚃⚄⚅")
                        rolls.forEach { append(it.emoji) }
                    }
                },
                onClick = {
                    haptic.performHapticFeedback(LongPress)
                    roll()
                },
                onLongClick = {
                    haptic.performHapticFeedback(LongPress)
                    reset()
                },
            )
            toggle(
                key = "strict-mode",
                title = "👮 StrictMode".asAnnotatedString(),
                message = "Flash the screen during a violation".asAnnotatedString(),
                checked = settings.strictMode,
                onCheckedChange = {
                    haptic.performHapticFeedback(LongPress)
                    update(settings.copy(strictMode = it))
                },
            )
            toggle(
                key = "uncaught-exception-handler",
                title = "🕵️ UncaughtExceptionHandler".asAnnotatedString(),
                message = "Catches unhandled thread exceptions".asAnnotatedString(),
                checked = settings.uncaughtExceptionHandler,
                onCheckedChange = {
                    haptic.performHapticFeedback(LongPress)
                    update(settings.copy(uncaughtExceptionHandler = it))
                },
            )
            entry(
                key = "crash",
                title = "💥 Crash!".asAnnotatedString(),
                message = "Simulate a process crash".asAnnotatedString(),
                onClick = { null!! },
            )

            category(key = "application", title = "Application", icon = Icons.Outlined.Info)
            entry(
                key = "package",
                title = "Package".asAnnotatedString(),
                message = data.packageName.asAnnotatedString(),
            )
            entry(
                key = "version-code",
                title = "Version code".asAnnotatedString(),
                message = data.versionCode.toString().asAnnotatedString(),
            )
            entry(
                key = "version-name",
                title = "Version name".asAnnotatedString(),
                message = data.versionName.asAnnotatedString(),
            )

            category(key = "device", title = "Device", icon = Icons.Default.Phone)
            entry(
                key = "make",
                title = "Make".asAnnotatedString(),
                message = data.deviceManufacturer.asAnnotatedString(),
            )
            entry(
                key = "model",
                title = "Model".asAnnotatedString(),
                message = data.deviceModel.asAnnotatedString(),
            )
            entry(
                key = "product",
                title = "Product".asAnnotatedString(),
                message = data.deviceProduct.asAnnotatedString(),
            )
            entry(
                key = "release",
                title = "Release".asAnnotatedString(),
                message = "${data.deviceRelease} (API ${data.deviceSdkInt})".asAnnotatedString(),
            )
            entry(
                key = "screen",
                title = "Screen".asAnnotatedString(),
                message = with(data.displayMetrics) { "$widthPixels $times $heightPixels @ $densityDpi dpi" }.asAnnotatedString(),
            )

            category(key = "timestamps", title = "Timestamps", icon = Icons.Default.DateRange)
            entry(
                key = "installed",
                title = "First install".asAnnotatedString(),
                message = data.firstInstallTime.toString().asAnnotatedString(),
            )
            entry(
                key = "updated",
                title = "Last update".asAnnotatedString(),
                message = data.lastUpdateTime.toString().asAnnotatedString(),
            )
            entry(
                key = "current",
                title = "Current".asAnnotatedString(),
                message = data.currentTime.toString().asAnnotatedString(),
            )
            item {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}

private fun LazyListScope.category(
    key: String,
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier.fillMaxWidth(),
): Unit = item(key = key, contentType = "category") {
    Row(
        modifier = modifier
            .testTag(key)
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(24.dp),
            imageVector = icon,
            tint = PlaygroundTheme.colorScheme.primary,
            contentDescription = null,
        )
        Text(
            text = title,
            color = PlaygroundTheme.colorScheme.primary,
            style = PlaygroundTheme.typography.titleMedium,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.entry(
    key: String,
    title: AnnotatedString,
    message: AnnotatedString,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onLongClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
): Unit = item(key = key, contentType = "entry") {
    Column(
        modifier = modifier
            .testTag(key)
            .run {
                if (onClick == null) combinedClickable(onClick = { }, onLongClick = onLongClick)
                else combinedClickable(onClick = onClick, onLongClick = onLongClick)
            }
            .padding(horizontal = 56.dp, vertical = 8.dp),
    ) {
        Text(
            text = title,
            style = PlaygroundTheme.typography.titleSmall,
        )
        Text(
            text = message,
            style = PlaygroundTheme.typography.bodySmall,
            fontFamily = Monospace,
        )
    }
}

private fun LazyListScope.toggle(
    key: String,
    title: AnnotatedString,
    message: AnnotatedString,
    checked: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onCheckedChange: ((Boolean) -> Unit) = {},
): Unit = item(key = key, contentType = "toggle") {
    Row(
        modifier = modifier
            .testTag(key)
            .clickable { onCheckedChange(!checked) }
            .padding(start = 56.dp, end = 32.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = modifier.weight(1F),
        ) {
            Text(
                text = title,
                style = PlaygroundTheme.typography.titleSmall,
            )
            Text(
                text = message,
                style = PlaygroundTheme.typography.bodySmall,
                fontFamily = Monospace,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Preview
@Composable
internal fun HomeScreenContentPreview() {
    HomeScreenContent(
        navigateToLicenses = {},
        rolls = persistentListOf(),
        settings = Settings(strictMode = true),
        data = HomeData(
            packageName = "preview.test",
            versionCode = 1,
            versionName = "1",
            deviceManufacturer = "manufacturer",
            deviceModel = "model",
            deviceProduct = "product",
            deviceSdkInt = 35,
            deviceRelease = "15",
            firstInstallTime = LocalDateTime.parse("2025-01-01T12:34:56"),
            lastUpdateTime = LocalDateTime.parse("2025-01-02T12:34:56"),
            currentTime = LocalDateTime.parse("2025-01-03T12:34:56"),
            displayMetrics = DisplayMetrics(
                widthPixels = 1080,
                heightPixels = 1920,
                densityDpi = DENSITY_XXXHIGH,
            ),
        ),
        roll = {},
        reset = {},
        update = {},
    )
}
