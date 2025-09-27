package fr.smarquis.playground.core.ui

import android.content.Context
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.AnnotatedString


@Composable
@ReadOnlyComposable
public fun resources(): Resources = LocalResources.current

@Composable
@ReadOnlyComposable
public fun context(): Context = LocalContext.current

public fun String.asAnnotatedString(): AnnotatedString = AnnotatedString(this)
