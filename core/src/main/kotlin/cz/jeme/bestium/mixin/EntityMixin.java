package cz.jeme.bestium.mixin;

import cz.jeme.bestium.EntityManagerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class EntityMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(final EntityType<?> type, final Level level, final CallbackInfo ci) {
        EntityManagerImpl.INSTANCE.trackEntityIfInjected((Entity) (Object) this);
    }
}
