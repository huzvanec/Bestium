package cz.jeme.bestium.inject.patch

import kotlin.reflect.KClass

internal sealed interface Patch {
    val classes: Set<KClass<*>>

    val computeFrames: Boolean
        get() = true

    fun transform()
}