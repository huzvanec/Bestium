package cz.jeme.bestium.mixin;

import net.kyori.adventure.text.Component;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.ServerLevelAccessor;
import org.bukkit.Bukkit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
abstract class ZombieMixin {
    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void mixinFinalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty, final EntitySpawnReason spawnReason, final SpawnGroupData spawnGroupData, final CallbackInfoReturnable<SpawnGroupData> cir) {
        Bukkit.broadcast(Component.text("Zombie spawned!"));
    }
}
