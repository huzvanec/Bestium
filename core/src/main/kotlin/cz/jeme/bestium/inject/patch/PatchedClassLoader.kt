package cz.jeme.bestium.inject.patch

import cz.jeme.bestium.util.BESTIUM_CLASS_LOADER
import cz.jeme.bestium.util.PAPER_CLASS_LOADER
import cz.jeme.bestium.util.PLATFORM_CLASS_LOADER

internal class PatchedClassLoader : ClassLoader(PLATFORM_CLASS_LOADER) {
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        var clazz: Class<*>?

        try {
            clazz = parent.loadClass(name)
            return clazz
        } catch (_: ClassNotFoundException) {
        }

        if (checkNonRecursive() && checkPaperLoader()) {
            // return or throw
            return BESTIUM_CLASS_LOADER.loadClass(name)
        }

        throw ClassNotFoundException(name)
    }

    // Rest of the class copied from https://github.com/xenondevs/Nova/blob/db8a2fef8e0b1258ad0a0df940238a092614ecca/nova/src/main/kotlin/xyz/xenondevs/nova/patch/PatchedClassLoader.kt

    /**
     * Checks the stacktrace for the PatchedClassLoader to prevent recursion.
     *
     * It's independent from [checkPaperLoader] as that requires loading a class, which would then cause recursion.
     */
    private fun checkNonRecursive(): Boolean {
        val stackTrace = Thread.currentThread().stackTrace
        for (i in 3..stackTrace.lastIndex) { // skip the first three elements: Thread.getStackTrace(), checkNonRecursive(), loadClass()
            val className = stackTrace[i].className

            // check whether the stack trace element is PatchedClassLoader, i.e. this is a recursive call
            if (className == /* Bestium: universal */ javaClass.name)
                return false

            // does not indicate a recursive call, but is the most common class loading deadlock cause,
            // so it is included here until the root cause is resolved (removal of PatchedClassLoader)
            if (className == "org.apache.logging.log4j.core.impl.ThrowableProxyHelper")
                return false
        }

        return true
    }

    /**
     * Checks that the class initiating the class loading process is loaded by the Paper Bundler (i.e a class that has been potentially patched).
     */
    private fun checkPaperLoader(): Boolean =
        findLoadingClass().classLoader == PAPER_CLASS_LOADER

    /**
     * Steps through the stack frames to find the first class that triggered a class loading process.
     */
    private fun findLoadingClass(): Class<*> {
        var takeNext = false
        var loadingClass: Class<*>? = null

        StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).forEach {
            var clazz = it.declaringClass

            if (takeNext) {
                loadingClass = clazz
                takeNext = false
            }

            while (clazz != null) {
                if (clazz == ClassLoader::class.java) {
                    takeNext = true
                    break
                }
                clazz = clazz.superclass
            }
        }

        return loadingClass!!
    }
}