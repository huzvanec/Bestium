package cz.jeme.bestium.inject.patch

import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodNode
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

internal abstract class MethodPatch : ClassPatch {

    protected val methodNode: MethodNode

    constructor(
        clazz: KClass<*>,
        method: MethodNode,
        computeFrames: Boolean = true,
        reversible: Boolean = false
    ) : super(clazz, computeFrames, reversible) {
        methodNode = method
    }

    constructor(
        clazz: KClass<*>,
        methodName: String,
        computeFrames: Boolean = true,
        reversible: Boolean = false
    ) : super(clazz, computeFrames, reversible) {
        methodNode = classWrapper.getMethod(methodName)!!
    }

    constructor(
        clazz: KClass<*>,
        methodName: String,
        desc: String,
        computeFrames: Boolean = true,
        reversible: Boolean = false
    ) : super(
        clazz,
        computeFrames,
        reversible
    ) {
        methodNode = classWrapper.getMethod(methodName, desc)!!
    }

    constructor(
        method: Method,
        computeFrames: Boolean = true,
        reversible: Boolean = false
    ) : super(method.declaringClass.kotlin, computeFrames, reversible) {
        methodNode = classWrapper.getMethod(method.name, Type.getMethodDescriptor(method))!!
    }

    constructor(
        function: KFunction<*>,
        computeFrames: Boolean = true,
        reversible: Boolean = false
    ) : this(function.javaMethod!!, computeFrames, reversible)

    constructor(method: Constructor<*>, computeFrames: Boolean = true, reversible: Boolean = false) : super(
        method.declaringClass.kotlin,
        computeFrames,
        reversible
    ) {
        methodNode = classWrapper.getMethod("<init>", Type.getConstructorDescriptor(method))!!
    }
}