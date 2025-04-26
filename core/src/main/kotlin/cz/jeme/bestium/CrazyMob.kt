//package cz.jeme.bestium
//
//import cz.jeme.bestium.entity.CustomMonster
//import net.minecraft.core.BlockPos
//import net.minecraft.sounds.SoundEvent
//import net.minecraft.sounds.SoundEvents
//import net.minecraft.world.damagesource.DamageSource
//import net.minecraft.world.entity.EntityType
//import net.minecraft.world.entity.ai.attributes.AttributeSupplier
//import net.minecraft.world.entity.ai.attributes.Attributes
//import net.minecraft.world.entity.ai.control.FlyingMoveControl
//import net.minecraft.world.entity.ai.goal.Goal
//import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation
//import net.minecraft.world.entity.ai.navigation.PathNavigation
//import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos
//import net.minecraft.world.entity.ai.util.HoverRandomPos
//import net.minecraft.world.entity.monster.Enemy
//import net.minecraft.world.entity.monster.Monster
//import net.minecraft.world.level.Level
//import java.util.*
//import kotlin.math.PI
//
//class CrazyMob(entityType: EntityType<out CrazyMob>, level: Level) : CustomMonster(entityType, level), Enemy {
//    companion object {
//        fun createAttributes(): AttributeSupplier.Builder = Monster.createMonsterAttributes()
//            .add(Attributes.MAX_HEALTH, 10.0)
//            .add(Attributes.FLYING_SPEED, 1.0)
//    }
//
//    init {
//        moveControl = FlyingMoveControl(this, 20, true)
//    }
//
//    override fun createNavigation(level: Level): PathNavigation {
//        return FlyingPathNavigation(this, level).apply {
//            setCanOpenDoors(false)
//            setCanFloat(false)
//            setRequiredPathLength(48F)
//        }
//    }
//
//    override fun getHurtSound(damageSource: DamageSource): SoundEvent? {
//        return SoundEvents.GOAT_SCREAMING_AMBIENT
//    }
//
//    override fun registerGoals() {
////        goalSelector.addGoal(0, RandomFloatGoal())
//    }
//
//    inner class RandomFloatGoal : Goal() {
//        init {
//            setFlags(EnumSet.of(Flag.MOVE))
//        }
//
//        override fun canUse(): Boolean {
//            return navigation.isDone && random.nextInt(10) == 0
//        }
//
//        override fun canContinueToUse(): Boolean {
//            return navigation.isInProgress
//        }
//
//        override fun start() {
//            val vect = getViewVector(0F)
//
//            navigation.moveTo(
//                navigation.createPath(
//                    BlockPos.containing(
//                        HoverRandomPos.getPos(
//                            this@CrazyMob,
//                            8,
//                            7,
//                            vect.x,
//                            vect.z,
//                            (PI / 2).toFloat(),
//                            3,
//                            1
//                        ) ?: AirAndWaterRandomPos.getPos(
//                            this@CrazyMob,
//                            8,
//                            4,
//                            -2,
//                            vect.x,
//                            vect.z,
//                            (PI / 2)
//                        ) ?: return
//                    ),
//                    1
//                ),
//                1.0
//            )
//        }
//    }
//}