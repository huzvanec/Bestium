package cz.jeme.bestium.inject.patch

import cz.jeme.bestium.inject.patch.impl.EntityDataPatch
import cz.jeme.bestium.inject.patch.impl.EntityTypePatch
import cz.jeme.bestium.inject.patch.impl.ReflectionPatch
import cz.jeme.bestium.util.NOVA_LOADED
import cz.jeme.bestium.util.PAPER_CLASS_LOADER
import cz.jeme.bestium.util.flushLoggingAndCrashJvm
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap
import net.bytebuddy.agent.ByteBuddyAgent
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import xyz.xenondevs.bytebase.INSTRUMENTATION
import xyz.xenondevs.bytebase.jvm.VirtualClassPath
import java.lang.instrument.ClassDefinition
import java.lang.ref.SoftReference
import java.lang.reflect.Field

private val PATCHES = linkedSetOf(
    ReflectionPatch,
    EntityTypePatch,
    EntityDataPatch
)

internal object Patcher {
    private val logger = ComponentLogger.logger("BestiumPatcher")
    private val instrumentation = ByteBuddyAgent.install()

    fun run() {
        try {
            run0()
        } catch (e: Throwable) {
            logger.error("An exception occurred while patching: ", e)
            flushLoggingAndCrashJvm()
        }
    }

    private fun run0() {
        if (NOVA_LOADED) logger.info("Nova detected, patching will be adjusted")

        logger.info("Patching server...")

        redefineModule()

        patch()

        injectPatchedClassLoader()

        revert()

        cleanup()

        logger.info("Patching successful")
    }

    private fun cleanup() {
        logger.info("Cleaning up")
        // cleanup reflection cache for ClassLoader so 
        // the parent field cannot be accessed anymore
        val reflectionDataRef = Class::class.java.getDeclaredField("reflectionData")
            .apply { isAccessible = true }[ClassLoader::class.java] as SoftReference<*>?
        reflectionDataRef?.clear()
    }

    private fun redefineModule() {
        val javaModule = Field::class.java.module
        val bestiumModule = javaClass.module

        INSTRUMENTATION.redefineModule(
            javaModule,
            emptySet(),
            emptyMap(),
            setOf(
                "jdk.internal.reflect",
                "java.lang"
            ).associateWith { setOf(bestiumModule) },
            emptySet(),
            emptyMap()
        )
    }

    private fun revert() {
        PATCHES
            .filterIsInstance<ClassPatch>()
            .filter(ClassPatch::reversible)
            .forEach { patch ->
                val clazz = patch.clazz.java
                logger.info("Reverting patch: ${clazz.name}")
                instrumentation.redefineClasses(
                    ClassDefinition(
                        clazz,
                        patch.initialBytecode!!
                    )
                )
            }
    }

    private fun patch() {
        logger.info("Collecting patches")
        val classes = Object2BooleanLinkedOpenHashMap<Class<*>>() // class -> computeFrames
        PATCHES.forEach { patch ->
            patch.classes
                .map { clazz -> clazz.java }
                .forEach { clazz ->
                    if (patch.computeFrames) classes[clazz] = true
                    else classes.putIfAbsent(clazz, false)
                }
        }
        PATCHES.forEachIndexed { index, patch ->
            logger.info("[${index + 1}/${PATCHES.size}] Loading patch: ${patch.javaClass.name}")
            patch.transform()
        }
        classes.object2BooleanEntrySet().mapIndexed { index, (clazz, computeFrames) ->
            logger.info("[${index + 1}/${classes.size}] Patching: ${clazz.name}")
            ClassDefinition(
                clazz,
                VirtualClassPath[clazz].assemble(computeFrames)
            )
        }.forEach { definition ->
            instrumentation.redefineClasses(definition)
        }
    }

    private fun injectPatchedClassLoader() {
        logger.info("Injecting patched class loader")
        val parentField = ClassLoader::class.java.getDeclaredField("parent")
            .apply { isAccessible = true }
        val injectToClassLoader =
            if (NOVA_LOADED) PAPER_CLASS_LOADER.parent // Nova's patched class loader
            else PAPER_CLASS_LOADER
        parentField.set(injectToClassLoader, PatchedClassLoader())
    }
}