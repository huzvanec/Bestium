package cz.jeme.bestium.inject.patch.impl

import cz.jeme.bestium.EntityManagerImpl
import cz.jeme.bestium.PluginSupportImpl
import cz.jeme.bestium.inject.patch.MultiPatch
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import org.objectweb.asm.Opcodes
import xyz.xenondevs.bytebase.asm.buildInsnList
import xyz.xenondevs.bytebase.jvm.VirtualClassPath
import xyz.xenondevs.bytebase.util.insertBeforeFirst
import kotlin.experimental.and
import kotlin.experimental.or

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
        VirtualClassPath[ENTITY_CTOR].instructions.insertBeforeFirst(buildInsnList {
            aLoad(0)
            aLoad(1)
            invokeStatic(::entityInit)
        }) { it.opcode == Opcodes.RETURN }

        // prevents updating data of injected entities
        VirtualClassPath[SET_ENTITY_DATA_PACKET_CTOR].instructions.insert(buildInsnList {
            iLoad(1)
            aLoad(2)
            invokeStatic(::modEntityData)
            aStore(2)
        })
    }

    private val injectedEntityIds = IntOpenHashSet()
    private val injectedLivingEntityIds = IntOpenHashSet()

    @JvmStatic
    fun entityInit(entity: Entity, type: EntityType<*>) {
        EntityManagerImpl.isInjected(type) || return
        injectedEntityIds.add(entity.id)
        if (entity is LivingEntity) injectedLivingEntityIds.add(entity.id)
    }

    @Suppress("UNCHECKED_CAST")
    private val DATA_SHARED_FLAGS_ID = Entity::class.java.getDeclaredField("DATA_SHARED_FLAGS_ID")
        .apply { isAccessible = true }[null] as EntityDataAccessor<Byte>

    private val ENTITY_DATA_ID_FILTER = { data: SynchedEntityData.DataValue<*> ->
        // https://minecraft.wiki/w/Java_Edition_protocol/Entity_metadata#Entity
        data.id <= 7
    }
    private val LIVING_ENTITY_DATA_ID_FILTER = { data: SynchedEntityData.DataValue<*> ->
        // https://minecraft.wiki/w/Java_Edition_protocol/Entity_metadata#Living_Entity
        data.id <= 14
    }

    @JvmStatic
    fun modEntityData(
        entityId: Int,
        entityData: List<SynchedEntityData.DataValue<*>>
    ): List<SynchedEntityData.DataValue<*>> {
        injectedEntityIds.contains(entityId) || return entityData

        val isLiving = injectedLivingEntityIds.contains(entityId)
        val newEntityData = entityData
            .filter(if (isLiving) LIVING_ENTITY_DATA_ID_FILTER else ENTITY_DATA_ID_FILTER)
            .toMutableList()

        val flagsIndex = entityData.indexOfFirst { it.id == 0 }

        var dataFlags: Byte = if (flagsIndex == -1) 0
        else newEntityData[flagsIndex].value as Byte

        fun setFlag(index: Int, value: Boolean) {
            dataFlags = if (value) (dataFlags or (1 shl index).toByte())
            else (dataFlags and (1 shl index).inv().toByte())
        }

        setFlag(Entity.FLAG_INVISIBLE, PluginSupportImpl.isBetterModelLoaded)

        val dataFlagsSynched = SynchedEntityData.DataValue.create(
            DATA_SHARED_FLAGS_ID,
            dataFlags
        )
        if (flagsIndex == -1) newEntityData += dataFlagsSynched
        else newEntityData[flagsIndex] = dataFlagsSynched

        return newEntityData
    }
}