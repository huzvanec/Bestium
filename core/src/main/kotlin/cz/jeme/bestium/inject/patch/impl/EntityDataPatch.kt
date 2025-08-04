package cz.jeme.bestium.inject.patch.impl

import cz.jeme.bestium.EntityManagerImpl
import cz.jeme.bestium.inject.patch.MultiPatch
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import org.objectweb.asm.Opcodes
import xyz.xenondevs.bytebase.asm.buildInsnList
import xyz.xenondevs.bytebase.jvm.VirtualClassPath
import xyz.xenondevs.bytebase.util.insertBeforeFirst

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
            invokeVirtual(Entity::getId)
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

    @JvmStatic
    fun entityInit(entityId: Int, type: EntityType<*>) {
        EntityManagerImpl.isInjected(type) || return
        injectedEntityIds.add(entityId)
    }

    @Suppress("UNCHECKED_CAST")
    private val DATA_SHARED_FLAGS_ID = Entity::class.java.getDeclaredField("DATA_SHARED_FLAGS_ID")
        .apply { isAccessible = true }[null] as EntityDataAccessor<Byte>

    private val EMPTY_INVISIBLE_DATA = listOf(
        SynchedEntityData.DataValue.create(
            DATA_SHARED_FLAGS_ID,
            (0 or (1 shl Entity.FLAG_INVISIBLE)).toByte()
        )
    )

    @JvmStatic
    fun modEntityData(
        entityId: Int,
        entityData: List<SynchedEntityData.DataValue<*>>
    ): List<SynchedEntityData.DataValue<*>> {
        return if (injectedEntityIds.contains(entityId)) EMPTY_INVISIBLE_DATA
        else entityData
    }
}