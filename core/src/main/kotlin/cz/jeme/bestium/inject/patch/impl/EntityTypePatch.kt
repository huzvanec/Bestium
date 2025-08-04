package cz.jeme.bestium.inject.patch.impl

import cz.jeme.bestium.EntityManagerImpl
import cz.jeme.bestium.inject.patch.MultiPatch
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import xyz.xenondevs.bytebase.asm.buildInsnList
import xyz.xenondevs.bytebase.jvm.VirtualClassPath
import java.util.*

private val ADD_ENTITY_PACKET_CTOR = ClientboundAddEntityPacket::class.java.getDeclaredConstructor(
    Int::class.javaPrimitiveType,
    UUID::class.java,
    Double::class.javaPrimitiveType,
    Double::class.javaPrimitiveType,
    Double::class.javaPrimitiveType,
    Float::class.javaPrimitiveType,
    Float::class.javaPrimitiveType,
    EntityType::class.java,
    Int::class.javaPrimitiveType,
    Vec3::class.java,
    Double::class.javaPrimitiveType
)

private val ENTITY_TOOLTIP_INFO_CTOR = HoverEvent.EntityTooltipInfo::class.java.getDeclaredConstructor(
    EntityType::class.java,
    UUID::class.java,
    Optional::class.java
)

internal object EntityTypePatch : MultiPatch(
    ClientboundAddEntityPacket::class,
    HoverEvent.EntityTooltipInfo::class
) {
    override fun transform() {
        VirtualClassPath[ADD_ENTITY_PACKET_CTOR].instructions.insert(buildInsnList {
            // Disguise custom mobs as backing types in ClientboundAddEntityPackets

            // 1 entity id
            // 2 entity uuid
            // 3-4 x
            // 5-6 y
            // 7-8 z
            // 9 xRot
            // 10 yRot
            aLoad(11) // 11 entity type
            invokeStatic(::remapType)
            aStore(11)
        })

        VirtualClassPath[ENTITY_TOOLTIP_INFO_CTOR].instructions.insert(buildInsnList {
            // Disguise custom mobs as backing types in entity hover events

            aLoad(1) // entity type
            invokeStatic(::remapType)
            aStore(1)
        })
    }

    @JvmStatic
    fun remapType(type: EntityType<*>): EntityType<*> {
        return EntityManagerImpl.getBackingType(type) ?: type
    }
}