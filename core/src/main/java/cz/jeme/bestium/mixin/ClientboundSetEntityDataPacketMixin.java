package cz.jeme.bestium.mixin;

import cz.jeme.bestium.EntityManagerImpl;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(ClientboundSetEntityDataPacket.class)
abstract class ClientboundSetEntityDataPacketMixin {
    @ModifyVariable(
            method = "<init>(ILjava/util/List;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private static List<SynchedEntityData.DataValue<?>> modifyPackedItems(final List<SynchedEntityData.DataValue<?>> original, final int id) {
        return EntityManagerImpl.INSTANCE.modifyEntityDataIfInjected(id, original);
    }
}
