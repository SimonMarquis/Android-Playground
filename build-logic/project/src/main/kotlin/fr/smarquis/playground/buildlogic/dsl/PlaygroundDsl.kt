// https://github.com/gradle/gradle/tree/master/platforms/core-configuration/kotlin-dsl/src/main/kotlin/org/gradle/kotlin/dsl
package fr.smarquis.playground.buildlogic.dsl

import org.gradle.api.DomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.HasMultipleValues
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.reflect.TypeOf
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

public fun Project.apply(plugin: String): Unit = pluginManager.apply(plugin)
public inline fun <reified T : Plugin<*>> Project.apply(): Unit = pluginManager.apply(T::class.java)

public inline fun <reified T : Any> Project.configure(noinline configuration: T.() -> Unit): Unit =
    extensions.configure(typeOf<T>(), configuration)

public inline fun <reified T : Any> ExtensionContainer.configure(noinline action: T.() -> Unit): Unit = configure(typeOf<T>(), action)
public inline fun <reified T : Any> ExtensionContainer.findByType(): T? = findByType(typeOf<T>())
public inline fun <reified T : Any> ExtensionContainer.getByType(): T = getByType(typeOf<T>())

public fun <T : Any> Property<T>.assign(value: T?): Unit = set(value)
public fun <T : Any> Property<T>.assign(value: Provider<out T>): Unit = set(value)
public fun <T : Any> HasMultipleValues<T>.assign(elements: Iterable<T>?): Unit = set(elements)
public fun <T : Any> HasMultipleValues<T>.assign(provider: Provider<out Iterable<T>>): Unit = set(provider)
public fun <K : Any, V : Any> MapProperty<K, V>.assign(entries: Map<out K, V>?): Unit = set(entries)
public fun <K : Any, V : Any> MapProperty<K, V>.assign(provider: Provider<out Map<out K, V>>): Unit = set(provider)

public inline fun <reified T : Task> TaskContainer.register(name: String, noinline configuration: T.() -> Unit): TaskProvider<T> =
    register(name, T::class.java, configuration)

public inline fun <reified S : Task> TaskCollection<in S>.withType(): TaskCollection<S> = withType(S::class.java)
public inline fun <reified S : Any> DomainObjectCollection<in S>.withType(): DomainObjectCollection<S> = withType(S::class.java)

public inline fun <reified T> typeOf(): TypeOf<T> = object : TypeOf<T>() {}
