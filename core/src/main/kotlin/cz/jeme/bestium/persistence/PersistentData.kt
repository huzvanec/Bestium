package cz.jeme.bestium.persistence

import cz.jeme.bestium.BestiumPlugin.createKey
import io.papermc.paper.persistence.PersistentDataContainerView
import io.papermc.paper.persistence.PersistentDataViewHolder
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType

class PersistentData<P : Any, C : Any> private constructor(
    val key: NamespacedKey,
    val type: PersistentDataType<P, C>
) {
    companion object {
        val BESTIUM_DATA_VERSION = PersistentData(createKey("data_version"), PersistentDataType.INTEGER)
        val BESTIUM_VARIANT = PersistentData(createKey("variant"), PersistentDataType.STRING)
        val BESTIUM_PENDING_MODEL = PersistentData(createKey("pending_model"), PersistentDataType.STRING)
    }

    operator fun get(container: PersistentDataContainerView): C? = container.get(key, type)

    operator fun get(holder: PersistentDataViewHolder) = get(holder.persistentDataContainer)

    operator fun set(container: PersistentDataContainer, value: C) {
        container.set(key, type, value)
    }

    operator fun set(holder: PersistentDataHolder, value: C) {
        set(holder.persistentDataContainer, value)
    }

    fun has(container: PersistentDataContainerView): Boolean = container.has(key, type)

    fun has(holder: PersistentDataViewHolder) = has(holder.persistentDataContainer)

    fun remove(container: PersistentDataContainer) {
        return container.remove(key)
    }

    fun remove(holder: PersistentDataHolder) = remove(holder.persistentDataContainer)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PersistentData<*, *>) return false

        if (key != other.key) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}