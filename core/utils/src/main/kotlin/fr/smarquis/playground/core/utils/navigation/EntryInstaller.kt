package fr.smarquis.playground.core.utils.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

// Typealias allows us to define a function "type" with a receiver (not supported with regular classes/interfaces)
// Extension or contextual function type is not allowed as a supertype.
public typealias EntryInstaller = EntryProviderScope<NavKey>.(backStack: BackStack) -> Unit
