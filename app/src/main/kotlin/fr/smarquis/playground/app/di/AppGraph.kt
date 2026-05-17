package fr.smarquis.playground.app.di

import android.app.Application
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import fr.smarquis.playground.app.PlaygroundActivity
import fr.smarquis.playground.app.PlaygroundApplication

@DependencyGraph(AppScope::class)
public interface AppGraph : ViewModelGraph {
    public fun inject(application: PlaygroundApplication)
    public fun inject(activity: PlaygroundActivity)

    @DependencyGraph.Factory
    public fun interface Factory {
        public fun create(@Provides application: Application): AppGraph
    }
}
