package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.misc.quack.ExtendedAreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.potion.PotionUtil;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin extends ThrownItemEntity {

    public PotionEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(method = "applySplashPotion", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/thrown/PotionEntity;squaredDistanceTo(Lnet/minecraft/entity/Entity;)D"))
    private Entity doPotionApplication(Entity entity) {
        if (!(entity instanceof LivingEntity target)) return entity;

        var stack = this.getStack();
        final var extraData = stack.getOr(PotionMixture.EXTRA_DATA, null);
        PotionUtil.getPotionEffects(stack).forEach(x -> MixinHooks.potionApplied(x, target, extraData));

        return entity;
    }

    @Redirect(method = "applySplashPotion", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectInstance;getDuration()I"))
    private int extendDuration(StatusEffectInstance instance) {
        var stack = this.getStack();
        int duration = instance.getDuration();

        if (stack.has(PotionMixture.EXTRA_DATA)) {
            duration *= stack.get(PotionMixture.EXTRA_DATA).getOr(PotionMixture.EXTEND_DURATION_BY, 1.0F);
        }

        return duration;
    }

    @ModifyArg(method = "applyLingeringPotion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private Entity addExtraData(Entity entity) {
        var stack = this.getStack();

        if (stack.has(PotionMixture.EXTRA_DATA)) {
            ((ExtendedAreaEffectCloudEntity) entity).affinity$setExtraPotionNbt(stack.get(PotionMixture.EXTRA_DATA));
        }

        return entity;
    }
}
