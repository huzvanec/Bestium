package cz.jeme.bestium.mixin;

import cz.jeme.bestium.EntityManagerImpl;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientboundAddEntityPacket.class)
abstract class ClientboundAddEntityPacketMixin {
    @ModifyVariable(
            method = "<init>(ILjava/util/UUID;DDDFFLnet/minecraft/world/entity/EntityType;ILnet/minecraft/world/phys/Vec3;D)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private static EntityType<?> modifyEntityType(EntityType<?> original) {
        return EntityManagerImpl.INSTANCE.remapType(original);
    }
}
