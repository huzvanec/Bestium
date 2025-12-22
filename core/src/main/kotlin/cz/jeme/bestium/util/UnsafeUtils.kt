package cz.jeme.bestium.util

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext

fun flushLoggingAndCrashJvm() {
    // Flush and stop all logging
    (LogManager.getContext(false) as LoggerContext).stop()
    // Terminate JVM
    Runtime.getRuntime().halt(-1)
}