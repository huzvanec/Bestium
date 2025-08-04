package cz.jeme.bestium.inject.patch

import xyz.xenondevs.bytebase.jvm.VirtualClassPath
import xyz.xenondevs.bytebase.util.internalName
import kotlin.reflect.KClass

internal abstract class ClassPatch(
    val clazz: KClass<*>,
    override val computeFrames: Boolean = true,
    val reversible: Boolean = false
) : Patch {
    var initialBytecode: ByteArray? = null
        private set

    init {
        if (reversible) {
            initialBytecode = (ClassLoader.getSystemResourceAsStream("${clazz.internalName}.class")
                ?: clazz.java.classLoader.getResourceAsStream("${clazz.internalName}.class")).readAllBytes()
        }
    }

    override val classes = setOf(clazz)

    protected val classWrapper = VirtualClassPath[clazz]
}