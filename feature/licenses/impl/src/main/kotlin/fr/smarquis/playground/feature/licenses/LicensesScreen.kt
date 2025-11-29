package fr.smarquis.playground.feature.licenses

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Horizontal
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Top
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily.Companion.Monospace
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.licensee.ArtifactDetail
import app.cash.licensee.SpdxLicense
import fr.smarquis.playground.core.ui.PlaygroundTheme
import fr.smarquis.playground.core.utils.navigation.BackStack
import fr.smarquis.playground.feature.licenses.UiState.Failure
import fr.smarquis.playground.feature.licenses.UiState.Loading
import fr.smarquis.playground.feature.licenses.UiState.Success
import kotlinx.collections.immutable.persistentMapOf
import java.io.FileNotFoundException
import kotlin.text.Typography.bullet

@Composable
internal fun LicensesScreen(
    backStack: BackStack,
) = LicensesScreen(
    navigateBack = { backStack.removeLastOrNull() },
)

@Composable
private fun LicensesScreen(
    modifier: Modifier = Modifier,
    viewModel: LicensesViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LicensesScreen(
        modifier = modifier,
        uiState = uiState,
        navigateBack = navigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LicensesScreen(
    uiState: UiState,
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier
            .testTag("licenses")
            .fillMaxSize()
            .background(PlaygroundTheme.colorScheme.background)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { LicensesScreenTopAppBar(navigateBack, scrollBehavior) },
        containerColor = Color.Transparent,
        contentColor = contentColorFor(PlaygroundTheme.colorScheme.background),
        contentWindowInsets = WindowInsets.safeDrawing.only(Horizontal),
    ) { contentPadding ->
        when (uiState) {
            Loading -> LicensesScreenLoading()
            is Failure -> LicensesScreenFailure(uiState, contentPadding)
            is Success -> LicensesScreenContent(uiState, contentPadding)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LicensesScreenTopAppBar(
    navigateBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.playground_feature_licenses_title),
                style = PlaygroundTheme.typography.headlineMedium,
                fontWeight = FontWeight.Thin,
                maxLines = 1,
                overflow = Ellipsis,
            )
        },
        windowInsets = WindowInsets.safeDrawing.only(Horizontal + Top),
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun LicensesScreenLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (LocalInspectionMode.current) CircularProgressIndicator(modifier = Modifier.width(64.dp), progress = { .5F })
        else CircularProgressIndicator(modifier = Modifier.width(64.dp))
    }
}

@Composable
private fun LicensesScreenFailure(failure: Failure, contentPadding: PaddingValues) {
    val scrollStateHorizontal = rememberScrollState()
    val scrollStateVertical = rememberScrollState()
    Card(
        modifier = Modifier
            .padding(contentPadding)
            .navigationBarsPadding()
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .horizontalScroll(scrollStateHorizontal)
                .verticalScroll(scrollStateVertical),
        ) {
            SelectionContainer {
                Text(
                    text = failure.cause.stackTraceToString().trimEnd(),
                    style = PlaygroundTheme.typography.labelSmall,
                    fontFamily = Monospace,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun LicensesScreenContent(
    uiState: Success,
    contentPadding: PaddingValues,
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        contentPadding = contentPadding,
        modifier = Modifier.testTag("licenses::list"),
    ) {
        uiState.licenses.forEach { (key, values) ->
            item(key = key, contentType = "header") {
                LicensesScreenHeader(key)
            }
            items(
                items = values,
                key = { """${it.groupId}:${it.artifactId}:${it.version}""" },
                contentType = { "item" },
            ) {
                LicensesScreenItem(it)
            }
        }
        item {
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun LicensesScreenHeader(key: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(key)
            .padding(vertical = 16.dp),
        verticalAlignment = CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(24.dp),
            imageVector = Icons.Outlined.Info,
            tint = PlaygroundTheme.colorScheme.primary,
            contentDescription = null,
        )
        Text(
            text = key,
            color = PlaygroundTheme.colorScheme.primary,
            style = PlaygroundTheme.typography.titleMedium,
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun LicensesScreenItem(it: ArtifactDetail) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = { expanded = !expanded })
            .padding(horizontal = 56.dp, vertical = 8.dp),
    ) {
        Column {
            Text(
                text = it.name ?: it.artifactId,
                style = PlaygroundTheme.typography.titleSmall,
            )
            Text(
                modifier = Modifier.basicMarquee(),
                text = "${it.artifactId}:${it.version}",
                style = PlaygroundTheme.typography.bodySmall,
                fontFamily = Monospace,
            )
            Text(
                modifier = Modifier.basicMarquee(),
                text = it.spdxLicenses.joinToString(" $bullet ") { it.name },
                style = PlaygroundTheme.typography.bodySmall,
                fontFamily = Monospace,
            )
        }
        LicensesScreenDropdown(it, expanded, onDismiss = { expanded = false })
    }
}

@Composable
private fun LicensesScreenDropdown(
    it: ArtifactDetail,
    expanded: Boolean,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        it.scm?.url?.let { url ->
            DropdownMenuItem(
                text = {
                    Column {
                        Text(
                            text = "SCM",
                            style = PlaygroundTheme.typography.titleSmall,
                        )
                        Text(
                            modifier = Modifier.basicMarquee(),
                            text = url,
                            style = PlaygroundTheme.typography.bodySmall,
                            fontFamily = Monospace,
                            maxLines = 1,
                        )
                    }
                },
                onClick = { uriHandler.openUri(url) },
            )
        }
        it.spdxLicenses.forEach {
            DropdownMenuItem(
                text = {
                    Column {
                        Text(
                            text = it.name,
                            style = PlaygroundTheme.typography.titleSmall,
                        )
                        Text(
                            text = it.url,
                            style = PlaygroundTheme.typography.bodySmall,
                            fontFamily = Monospace,
                            maxLines = 1,
                        )
                    }
                },
                onClick = { uriHandler.openUri(it.url) },
            )
        }
    }
}

@Preview
@Composable
internal fun LicensesScreenContentLoadingPreview() {
    LicensesScreen(
        uiState = Loading,
        navigateBack = {},
    )
}

@Preview
@Composable
internal fun LicensesScreenContentFailurePreview() {
    LicensesScreen(
        uiState = Failure(FileNotFoundException().apply { stackTrace = stackTrace.take(1).toTypedArray() }),
        navigateBack = {},
    )
}

private val Apache_20 = SpdxLicense(
    identifier = "Apache-2.0",
    name = "Apache License 2.0",
    url = "https://www.apache.org/licenses/LICENSE-2.0",
)

private val MIT = SpdxLicense(
    identifier = "MIT",
    name = "MIT License",
    url = "https://opensource.org/license/mit/",
)

@Preview
@Composable
internal fun LicensesScreenContentPreview() {
    LicensesScreen(
        uiState = Success(
            persistentMapOf(
                "fr.smarquis" to listOf(
                    ArtifactDetail(groupId = "fr.smarquis", artifactId = "foo", name = "Foo", version = "1.0.0", spdxLicenses = setOf(MIT)),
                    ArtifactDetail(
                        groupId = "fr.smarquis",
                        artifactId = "bar",
                        name = "Bar",
                        version = "1.2.3",
                        spdxLicenses = setOf(Apache_20),
                    ),
                ),
                "org.example" to listOf(
                    ArtifactDetail(groupId = "org.example", artifactId = "baz", name = "Baz", version = "1.0.0", spdxLicenses = setOf(MIT)),
                    ArtifactDetail(
                        groupId = "org.example",
                        artifactId = "qux",
                        name = "Qux",
                        version = "1.2.3",
                        spdxLicenses = setOf(Apache_20),
                    ),
                ),
            ),
        ),
        navigateBack = {},
    )
}
