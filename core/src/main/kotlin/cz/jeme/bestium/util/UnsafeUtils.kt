@file:Suppress("DEPRECATION")

package cz.jeme.bestium.util

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import sun.misc.Unsafe
import java.lang.reflect.Field

private val unsafe by lazy {
    Unsafe::class.java.getDeclaredField("theUnsafe")
        .apply { isAccessible = true }[null] as Unsafe
}

fun Field.setStaticFinal(value: Any?) = setFinal(
    unsafe.staticFieldBase(this),
    value
)

fun Field.setFinal(obj: Any?, value: Any?) = unsafe.putObject(
    obj,
    unsafe.staticFieldOffset(this),
    value
)

fun flushLoggingAndCrashJvm() {
    // Flush and stop all logging
    (LogManager.getContext(false) as LoggerContext).stop()
    // Terminate JVM
    Runtime.getRuntime().halt(-1)
}