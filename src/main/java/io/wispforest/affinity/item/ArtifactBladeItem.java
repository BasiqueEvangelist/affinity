package io.wispforest.affinity.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.object.AffinityEntityAttributes;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.ops.TextOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ArtifactBladeItem extends SwordItem {

    public static final AffinityEntityAddon.DataKey<Boolean> DID_CRIT = AffinityEntityAddon.DataKey.withDefaultConstant(false);

    private static final NbtKey<Long> ABILITY_START_TIME = new NbtKey<>("AbilityStartTime", NbtKey.Type.LONG);
    private static final EntityAttributeModifier DAMAGE_MODIFIER = new EntityAttributeModifier(UUID.fromString("3fb1b365-3b7b-421e-9237-6d5c92d97625"), "Aethum Ability Damage Boost", 2, EntityAttributeModifier.Operation.ADDITION);

    public final Tier tier;
    private final Multimap<EntityAttribute, EntityAttributeModifier> modifiers;
    private final Multimap<EntityAttribute, EntityAttributeModifier> modifiersWithDamage;

    public ArtifactBladeItem(Tier tier) {
        super(tier, 0, tier.data.attackSpeed, AffinityItems.settings(AffinityItemGroup.MAIN).maxCount(1).rarity(tier.data.rarity));
        this.tier = tier;

        var modifiers = ImmutableMultimap.<EntityAttribute, EntityAttributeModifier>builder().putAll(super.getAttributeModifiers(EquipmentSlot.MAINHAND));

        if (this.tier == Tier.ASTRAL) {
            modifiers.put(AffinityEntityAttributes.MAX_AETHUM, new EntityAttributeModifier(UUID.randomUUID(), "i hate attributes", 1, EntityAttributeModifier.Operation.MULTIPLY_TOTAL))
                    .put(AffinityEntityAttributes.NATURAL_AETHUM_REGEN_SPEED, new EntityAttributeModifier(UUID.randomUUID(), "i hate attributes episode 2", 3, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        this.modifiers = modifiers.build();

        modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, DAMAGE_MODIFIER);
        this.modifiersWithDamage = modifiers.build();
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND
                ? stack.has(ABILITY_START_TIME) ? this.modifiersWithDamage : this.modifiers
                : super.getAttributeModifiers(slot);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var playerStack = user.getStackInHand(hand);
        if (playerStack.has(ABILITY_START_TIME)) return TypedActionResult.pass(playerStack);

        var aethum = AffinityComponents.PLAYER_AETHUM.get(user);
        if (!(aethum.tryConsumeAethum(aethum.maxAethum() * this.tier.data.abilityAethumCost))) return TypedActionResult.pass(playerStack);

        playerStack.put(ABILITY_START_TIME, world.getTime());
        return TypedActionResult.success(playerStack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (getAbilityTicks(world, stack) > this.tier.data.abilityDuration) {
            stack.delete(ABILITY_START_TIME);

            if (!(entity instanceof PlayerEntity player)) return;
            player.getItemCooldownManager().set(this, this.tier.data.abilityCooldown);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.affinity.staff.tooltip.consumption_per_use", Text.literal((int) (this.tier.data.abilityAethumCost * 100) + "%")));
    }

    public int abilityDuration() {
        return this.tier.data.abilityDuration;
    }

    public static boolean isBladeWithActiveAbility(World world, ItemStack stack, int minTier) {
        if (!(stack.getItem() instanceof ArtifactBladeItem blade)) return false;
        return getAbilityTicks(world, stack) >= 0 && blade.tier.ordinal() >= minTier;
    }

    public static int getAbilityTicks(World world, ItemStack stack) {
        if (!stack.has(ABILITY_START_TIME)) return -1;
        return (int) (world.getTime() - stack.get(ABILITY_START_TIME));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (stack.getItem() instanceof ArtifactBladeItem blade && blade.tier == Tier.ASTRAL && attacker instanceof PlayerEntity player) {
            target.setHealth(0);
            target.onDeath(DamageSource.player(player));
            return true;
        } else {
            return super.postHit(stack, target, attacker);
        }
    }

    private static MutableText makeInfiniteText() {
        var chars = Language.getInstance().get("text.affinity.infinite_attack_damage").toCharArray();
        float baseHue = (float) (System.currentTimeMillis() % 3000d) / 3000f;

        var outText = Text.literal(" ");

        for (int i = 0; i < chars.length; i++) {
            float hue = baseHue - i * .05f;
            if (hue < 0) hue += 1f;

            outText.append(TextOps.withColor(String.valueOf(chars[i]), MathHelper.hsvToRgb(hue, .8f, 1)));
        }

        return outText.append(Text.literal(" "));
    }

    @Environment(EnvType.CLIENT)
    private static void registerTooltipAddition() {
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (!(stack.getItem() instanceof ArtifactBladeItem blade) || blade.tier != Tier.ASTRAL) return;

            for (int i = 0; i < lines.size(); i++) {
                var line = lines.get(i);
                if (!(line.getContent() instanceof LiteralTextContent) || line.getSiblings().isEmpty()) continue;


                var sibling = line.getSiblings().get(0);
                if (!(sibling.getContent() instanceof TranslatableTextContent modifierTranslatable)) continue;
                if (modifierTranslatable.getArgs().length < 2) continue;

                if (!(modifierTranslatable.getArgs()[1] instanceof Text text) || !(text.getContent() instanceof TranslatableTextContent translatable) || !translatable.getKey().startsWith("attribute.name.generic.attack_damage")) {
                    continue;
                }

                lines.set(i, makeInfiniteText().append(Text.translatable(EntityAttributes.GENERIC_ATTACK_DAMAGE.getTranslationKey()).formatted(Formatting.DARK_GREEN)));
                return;
            }
        });
    }

    static {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            registerTooltipAddition();
        }
    }

    public enum Tier implements ToolMaterial {
        FORGOTTEN(new TierData(500, 2, 20, 7, 7f, -2.4f, Rarity.UNCOMMON, 100, 300, .5f)),
        STABILIZED(new TierData(1000, 3, 25, 8, 10f, -2.4f, Rarity.UNCOMMON, 100, 300, .6f)),
        STRENGTHENED(new TierData(1500, 4, 35, 12, 12f, -2f, Rarity.RARE, 160, 400, .6f)),
        SUPERIOR(new TierData(3000, 5, 40, 15, 15f, -1.8f, Rarity.EPIC, 200, 800, .8f)),
        ASTRAL(new TierData(69000, 6, 100, 6969, 75f, 21f, Rarity.EPIC, 200, 800, 1f));

        private final TierData data;

        Tier(TierData data) {
            this.data = data;
        }

        @Override
        public int getDurability() {
            return this.data.durability;
        }

        @Override
        public float getMiningSpeedMultiplier() {
            return this.data.miningSpeedMultiplier;
        }

        @Override
        public float getAttackDamage() {
            return this.data.attackDamage;
        }

        @Override
        public int getMiningLevel() {
            return this.data.miningLevel;
        }

        @Override
        public int getEnchantability() {
            return this.data.enchantability;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }

        private record TierData(int durability, int miningLevel, int enchantability, float attackDamage, float miningSpeedMultiplier, float attackSpeed,
                                Rarity rarity, int abilityDuration, int abilityCooldown, float abilityAethumCost) {}
    }
}
