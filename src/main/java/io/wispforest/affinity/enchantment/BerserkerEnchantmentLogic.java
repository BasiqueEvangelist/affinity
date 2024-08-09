package io.wispforest.affinity.enchantment;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.callback.ItemEquipEvents;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.object.AffinityEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.RegistryKeys;

import java.util.UUID;

public class BerserkerEnchantmentLogic {
    private static final EntityAttributeModifier HEALTH_ADDITION = new EntityAttributeModifier(
        Affinity.id("berserker_health_boost"), 10, EntityAttributeModifier.Operation.ADD_VALUE);

    public static final AffinityEntityAddon.DataKey<Boolean> BERSERK_KEY = AffinityEntityAddon.DataKey.withDefaultConstant(false);

    public static void initialize() {
        ItemEquipEvents.EQUIP.register((entity, slot, stack) -> {
            var ench = entity.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(AffinityEnchantments.BERSERKER).orElseThrow();

            if (!ench.value().slotMatches(slot)) return;

            if (AbsoluteEnchantmentLogic.hasCompleteArmor(entity, ench)) {
                AffinityEntityAddon.setData(entity, BERSERK_KEY, true);

                if (!healthAttribute(entity).hasModifier(HEALTH_ADDITION.id())) {
                    healthAttribute(entity).addTemporaryModifier(HEALTH_ADDITION);
                }
            }
        });

        ItemEquipEvents.UNEQUIP.register((entity, slot, stack) -> {
            var ench = entity.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(AffinityEnchantments.BERSERKER).orElseThrow();

            if (!ench.value().slotMatches(slot)) return;

            if (!AbsoluteEnchantmentLogic.hasCompleteArmor(entity, ench)) {
                AffinityEntityAddon.setData(entity, BERSERK_KEY, false);

                healthAttribute(entity).removeModifier(HEALTH_ADDITION.id());
                entity.damage(entity.getDamageSources().outOfWorld(), Float.MIN_NORMAL);
            }
        });
    }

    private static EntityAttributeInstance healthAttribute(LivingEntity entity) {
        return entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
    }
}
