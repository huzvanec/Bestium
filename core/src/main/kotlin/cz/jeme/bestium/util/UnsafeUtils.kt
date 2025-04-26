@file:Suppress("DEPRECATION")

package cz.jeme.bestium.util

import sun.misc.Unsafe
import java.lang.reflect.Field

private val unsafe by lazy {
    Unsafe::class.java.getDeclaredField("theUnsafe")
        .apply { isAccessible = true }
        .get(null) as Unsafe
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