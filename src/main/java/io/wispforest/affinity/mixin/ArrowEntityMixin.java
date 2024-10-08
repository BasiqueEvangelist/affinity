package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.potion.PotionMixture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArrowEntity.class)
public class ArrowEntityMixin {

    @Shadow private Potion potion;

    private NbtCompound affinity$extraPotionNbt;

    @Inject(method = "initFromStack", at = @At("RETURN"))
    private void addExtraData(ItemStack stack, CallbackInfo ci) {
        this.affinity$extraPotionNbt = stack.getOr(PotionMixture.EXTRA_DATA, null);
    }

    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/Potion;getEffects()Ljava/util/List;"))
    private void doPotionApplication(LivingEntity target, CallbackInfo ci) {
        this.potion.getEffects().forEach(x -> MixinHooks.potionApplied(x, target, this.affinity$extraPotionNbt));
    }

    @ModifyArg(method = "onHit", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"), index = 0)
    private int addDuration(int duration) {
        duration *= this.affinity$extraPotionNbt.getOr(PotionMixture.EXTEND_DURATION_BY, 1.0F);

        return duration;
    }

}
