package fr.smarquis.playground.core.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import fr.smarquis.playground.core.di.qualifier.Dispatcher
import fr.smarquis.playground.core.di.qualifier.Dispatcher.Type.Default
import fr.smarquis.playground.core.di.qualifier.Dispatcher.Type.IO
import fr.smarquis.playground.core.di.qualifier.Dispatcher.Type.Main
import fr.smarquis.playground.core.di.qualifier.Dispatcher.Type.MainImmediate
import fr.smarquis.playground.core.di.qualifier.Dispatcher.Type.Unconfined
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

@ContributesTo(AppScope::class)
@BindingContainer
public object CoroutinesBindings {

    @Provides @Dispatcher(Main)
    public fun providesMain(): CoroutineContext = Dispatchers.Main

    @Provides @Dispatcher(MainImmediate)
    public fun providesMainImmediate(): CoroutineContext = Dispatchers.Main.immediate

    @Provides @Dispatcher(Default)
    public fun providesDefault(): CoroutineContext = Dispatchers.Default

    @Provides @Dispatcher(IO)
    public fun providesIo(): CoroutineContext = Dispatchers.IO

    @Provides @Dispatcher(Unconfined)
    public fun providesUnconfined(): CoroutineContext = Dispatchers.Unconfined

    @Provides
    public fun providesProvider(): CoroutineContextProvider = object : CoroutineContextProvider {}

}

public interface CoroutineContextProvider {
    public fun main(): CoroutineContext = Dispatchers.Main
    public fun mainImmediate(): CoroutineContext = Dispatchers.Main.immediate
    public fun default(): CoroutineContext = Dispatchers.Default
    public fun io(): CoroutineContext = Dispatchers.IO
    public fun unconfined(): CoroutineContext = Dispatchers.Unconfined
}

public fun CoroutineContext.asCoroutineContextProvider(): CoroutineContextProvider = object : CoroutineContextProvider {
    override fun main(): CoroutineContext = this@asCoroutineContextProvider
    override fun mainImmediate(): CoroutineContext = this@asCoroutineContextProvider
    override fun default(): CoroutineContext = this@asCoroutineContextProvider
    override fun io(): CoroutineContext = this@asCoroutineContextProvider
    override fun unconfined(): CoroutineContext = this@asCoroutineContextProvider
}
