package fr.smarquis.playground.core.di.qualifier

import javax.inject.Qualifier

@Qualifier
public annotation class Dispatcher(val type: Type) {
    public enum class Type { Default, IO, Main, MainImmediate, Unconfined }
}
