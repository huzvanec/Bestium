package cz.jeme.bestium.mixin;

import cz.jeme.bestium.EntityManagerImpl;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HoverEvent.EntityTooltipInfo.class)
abstract class EntityTooltipInfoMixin {
    @ModifyVariable(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Ljava/util/UUID;Ljava/util/Optional;)V",
            at = @At("HEAD"),
            argsOnly = true,
            name = "type"
    )
    private static EntityType<?> modifyEntityType(final EntityType<?> type) {
        return EntityManagerImpl.INSTANCE.remapType(type);
    }
}
