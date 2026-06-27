package fr.smarquis.playground.app

import android.annotation.SuppressLint
import android.app.Application
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
@BindingContainer
public object PlaygroundBindings {

    @Provides
    @SuppressLint("UnsafeCast")
    public fun providesPlaygroundApplication(app: Application): PlaygroundApplication = app as PlaygroundApplication

}
