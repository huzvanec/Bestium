package cz.jeme.bestium.inject.patch

import kotlin.reflect.KClass

internal abstract class MultiPatch(
    final override val classes: Set<KClass<*>>,
    override val computeFrames: Boolean = true
) : Patch {
    constructor(vararg classes: KClass<*>, computeFrames: Boolean = true) : this(
        classes.toSet(), computeFrames
    )
}