package cz.jeme.bestium.inject.patch.impl

import cz.jeme.bestium.inject.patch.MultiPatch
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import xyz.xenondevs.bytebase.asm.buildInsnList
import xyz.xenondevs.bytebase.jvm.VirtualClassPath

private val SET_ENTITY_DATA_PACKET_CTOR = ClientboundSetEntityDataPacket::class.java.getDeclaredConstructor(
    Int::class.javaPrimitiveType,
    List::class.java,
)

private val ENTITY_CTOR = Entity::class.java.getDeclaredConstructor(
    EntityType::class.java,
    Level::class.java
)

internal object EntityDataPatch : MultiPatch(Entity::class, ClientboundSetEntityDataPacket::class) {
    override fun transform() {
        VirtualClassPath[ENTITY_CTOR].instructions.add(buildInsnList {
            aLoad(0)
            invokeVirtual(Entity::getId)
            invokeStatic(::addInjectedEntity)
        })

        // prevents updating data of injected entities
        VirtualClassPath[SET_ENTITY_DATA_PACKET_CTOR].instructions.insert(buildInsnList {
            iLoad(1)
            aLoad(2)
            invokeStatic(::modEntityData)
            aStore(2)
        })
    }

    private val injectedEntityIds = IntOpenHashSet()

    @JvmStatic
    fun addInjectedEntity(entityId: Int) {
        injectedEntityIds.add(entityId)
    }

    @JvmStatic
    fun modEntityData(
        entityId: Int,
        list: List<SynchedEntityData.DataValue<*>>
    ): List<SynchedEntityData.DataValue<*>> {
        return if (injectedEntityIds.contains(entityId)) emptyList() else list
    }
}