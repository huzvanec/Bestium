package cz.jeme.bestium.hooks.bettermodel

import cz.jeme.bestium.api.Bestium
import kr.toxicity.model.api.BetterModel
import kr.toxicity.model.api.bukkit.BetterModelBukkit
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter
import kr.toxicity.model.api.data.ModelAsset
import kr.toxicity.model.api.event.ModelAssetsEvent
import kr.toxicity.model.api.event.ModelEvent
import kr.toxicity.model.api.event.ModelEventApplication
import org.bukkit.entity.Entity
import java.io.IOException
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.isDirectory
import kotlin.reflect.KClass


class BetterModelHook {
    private val plugin = Bestium.getPlugin()
    private val logger = plugin.componentLogger

    @Suppress("ObjectLiteralToLambda")
    private val eventApp = object : ModelEventApplication {
        override fun isEnabled(): Boolean = plugin.isEnabled
    }

    fun applyModel(entity: Entity, modelName: String) {
        BetterModel.model(modelName).map { renderer ->
            renderer.create(BukkitAdapter.adapt(entity))
        }
    }

    private fun onModelAssetsEvent(event: ModelAssetsEvent) {
        Bestium.getInjector().injections.values
            .flatMap { inj -> inj.variants.values }
            .forEach { variant ->
                event.addAsset(
                    ModelAsset.of(variant.modelName) { variant.modelUrl.openStream() }
                )
            }
    }

    fun register() {
        // remove legacy models folder
        val betterModelData = BetterModelBukkit.platform().dataFolder().toPath()
        val bestiumLegacyModels = betterModelData.resolve("models/.bestium")

        try {
            if (bestiumLegacyModels.isDirectory()) {
                @OptIn(ExperimentalPathApi::class)
                bestiumLegacyModels.deleteRecursively()
            }
        } catch (e: IOException) {
            logger.error("Failed to remove legacy models folder: '$bestiumLegacyModels'", e)
        }

        // register events
        fun <T : ModelEvent> sub(clazz: KClass<T>, consumer: (T) -> Unit) {
            BetterModel.eventBus().subscribe(eventApp, clazz.java, consumer)
        }

        sub(ModelAssetsEvent::class, ::onModelAssetsEvent)
    }
}
