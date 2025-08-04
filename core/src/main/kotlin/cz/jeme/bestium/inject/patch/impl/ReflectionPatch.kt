@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package cz.jeme.bestium.inject.patch.impl

import cz.jeme.bestium.inject.patch.MethodPatch
import jdk.internal.reflect.Reflection
import xyz.xenondevs.bytebase.asm.buildInsnList

internal object ReflectionPatch : MethodPatch(Reflection::filterFields, reversible = true) {
    override fun transform() {
        methodNode.instructions = buildInsnList {
            aLoad(1)
            areturn()
        }
    }
}