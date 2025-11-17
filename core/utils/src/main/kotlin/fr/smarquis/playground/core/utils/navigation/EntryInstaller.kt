package fr.smarquis.playground.core.utils.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

public typealias EntryInstaller = EntryProviderScope<NavKey>.(SnapshotStateList<NavKey>) -> Unit
