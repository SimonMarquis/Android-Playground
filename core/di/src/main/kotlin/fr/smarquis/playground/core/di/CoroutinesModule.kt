package fr.smarquis.playground.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.smarquis.playground.core.di.qualifier.Dispatcher
import fr.smarquis.playground.core.di.qualifier.Dispatcher.Type.Default
import fr.smarquis.playground.core.di.qualifier.Dispatcher.Type.IO
import fr.smarquis.playground.core.di.qualifier.Dispatcher.Type.Main
import fr.smarquis.playground.core.di.qualifier.Dispatcher.Type.MainImmediate
import fr.smarquis.playground.core.di.qualifier.Dispatcher.Type.Unconfined
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

@Module @InstallIn(SingletonComponent::class)
internal object CoroutinesModule {

    @Provides @Dispatcher(Main)
    fun providesMain(): CoroutineContext = Dispatchers.Main

    @Provides @Dispatcher(MainImmediate)
    fun providesMainImmediate(): CoroutineContext = Dispatchers.Main.immediate

    @Provides @Dispatcher(Default)
    fun providesDefault(): CoroutineContext = Dispatchers.Default

    @Provides @Dispatcher(IO)
    fun providesIo(): CoroutineContext = Dispatchers.IO

    @Provides @Dispatcher(Unconfined)
    fun providesUnconfined(): CoroutineContext = Dispatchers.Unconfined

    @Provides
    fun providesProvider(): CoroutineContextProvider = object : CoroutineContextProvider {}

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
