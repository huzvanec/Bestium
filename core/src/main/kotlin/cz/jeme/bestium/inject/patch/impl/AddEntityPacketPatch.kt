package cz.jeme.bestium.inject.patch.impl

import cz.jeme.bestium.EntityManagerImpl
import cz.jeme.bestium.inject.patch.MethodPatch
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import xyz.xenondevs.bytebase.asm.buildInsnList
import java.util.*

private val CTOR = ClientboundAddEntityPacket::class.java.getDeclaredConstructor(
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

internal object AddEntityPacketPatch : MethodPatch(CTOR) {
    override fun transform() {
        methodNode.instructions.insert(buildInsnList {
            aLoad(11)
            invokeStatic(::remapEntityType)
            aStore(11)
        })
    }

    @JvmStatic
    fun remapEntityType(type: EntityType<*>): EntityType<*> {
        return EntityManagerImpl.getBackingType(type) ?: type
    }
}