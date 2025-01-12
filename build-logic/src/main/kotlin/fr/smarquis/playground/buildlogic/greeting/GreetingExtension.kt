package fr.smarquis.playground.buildlogic.greeting

import org.gradle.api.provider.Property

public abstract class GreetingExtension public constructor() {
    public abstract val who: Property<String>
}
