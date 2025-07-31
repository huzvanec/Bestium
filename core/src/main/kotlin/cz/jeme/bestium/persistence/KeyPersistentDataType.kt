package cz.jeme.bestium.persistence

import net.kyori.adventure.key.Key
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

object KeyPersistentDataType : PersistentDataType<String, Key> {
    override fun getPrimitiveType() = String::class.java

    override fun getComplexType() = Key::class.java

    override fun toPrimitive(
        complex: Key,
        context: PersistentDataAdapterContext
    ) = complex.asString()

    override fun fromPrimitive(
        primitive: String,
        context: PersistentDataAdapterContext
    ) = Key.key(primitive)
}