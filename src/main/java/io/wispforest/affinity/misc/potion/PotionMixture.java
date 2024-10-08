package io.wispforest.affinity.misc.potion;

import com.google.common.collect.ImmutableList;
import io.wispforest.owo.nbt.NbtKey;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Container for a potion. This could be a {@link net.minecraft.potion.Potion} contained in {@link net.minecraft.registry.Registries#POTION}
 * or simply a list of {@link net.minecraft.entity.effect.StatusEffectInstance}s
 */
public class PotionMixture {

    public static final NbtKey<NbtCompound> EXTRA_DATA = new NbtKey<>("ExtraPotionData", NbtKey.Type.COMPOUND);

    public static final NbtKey<Float> EXTEND_DURATION_BY = new NbtKey<>("ExtendDurationBy", NbtKey.Type.FLOAT);

    public static final PotionMixture EMPTY = new PotionMixture(Potions.EMPTY, ImmutableList.of(), true, null);
    public static final Potion DUBIOUS_POTION = new Potion("dubious");

    private final Potion basePotion;
    private final List<StatusEffectInstance> effects;
    private final boolean pure;
    private final int color;
    private NbtCompound extraNbt;

    public PotionMixture(Potion basePotion, NbtCompound extraNbt) {
        this.basePotion = basePotion;
        this.effects = ImmutableList.of();
        this.pure = true;
        this.extraNbt = extraNbt;

        final var colorEffects = new ArrayList<>(effects);
        if (basePotion != Potions.EMPTY) colorEffects.addAll(basePotion.getEffects());

        this.color = PotionUtil.getColor(colorEffects);
    }

    public PotionMixture(Potion basePotion, List<StatusEffectInstance> effects, boolean pure, NbtCompound extraNbt) {
        this.basePotion = basePotion;
        this.effects = ImmutableList.copyOf(effects);
        this.pure = pure;
        this.extraNbt = extraNbt;

        final var colorEffects = new ArrayList<>(effects);
        if (basePotion != Potions.EMPTY) colorEffects.addAll(basePotion.getEffects());

        this.color = PotionUtil.getColor(colorEffects);
    }

    public PotionMixture mix(PotionMixture other) {

        if (this.equals(other)) return this;

        final var effects = new ArrayList<>(this.effects);
        effects.addAll(other.effects);
        effects.addAll(basePotion.getEffects());
        effects.addAll(other.basePotion.getEffects());

        return new PotionMixture(Potions.EMPTY, effects, false, null);
    }

    public static PotionMixture fromStack(ItemStack stack) {
        final var potion = PotionUtil.getPotion(stack);
        final var effects = PotionUtil.getCustomPotionEffects(stack);

        return new PotionMixture(potion, effects, true, stack.getOr(EXTRA_DATA, null));
    }

    public static PotionMixture fromNbt(NbtCompound nbt) {

        var potion = Potions.EMPTY;
        var effects = new ArrayList<StatusEffectInstance>();

        if (nbt.contains("Potion", NbtElement.COMPOUND_TYPE)) {
            final var potionNbt = nbt.getCompound("Potion");
            potion = Registries.POTION.get(Identifier.tryParse(potionNbt.getString("id")));
        }

        if (nbt.contains("Effects", NbtElement.LIST_TYPE)) {
            final var effectsNbt = nbt.getList("Effects", NbtElement.COMPOUND_TYPE);
            for (var effect : effectsNbt) {
                effects.add(StatusEffectInstance.fromNbt((NbtCompound) effect));
            }
        }

        return new PotionMixture(potion, effects, nbt.getBoolean("Pure"), nbt.getOr(EXTRA_DATA, null));
    }

    public NbtCompound toNbt() {
        final var nbt = new NbtCompound();

        if (basePotion != Potions.EMPTY) {
            final var potionNbt = new NbtCompound();
            potionNbt.putString("id", Registries.POTION.getId(basePotion).toString());

            nbt.put("Potion", potionNbt);
        }

        if (!effects.isEmpty()) {
            final var effectsNbt = new NbtList();
            for (var effect : effects) {
                effectsNbt.add(effect.writeNbt(new NbtCompound()));
            }

            nbt.put("Effects", effectsNbt);
        }

        nbt.putBoolean("Pure", pure);

        nbt.putIfNotNull(EXTRA_DATA, extraNbt);

        return nbt;
    }

    public ItemStack toStack() {
        final var stack = new ItemStack(Items.POTION);

        if (pure) {
            if (basePotion != Potions.EMPTY) PotionUtil.setPotion(stack, basePotion);
            if (!effects.isEmpty()) PotionUtil.setCustomPotionEffects(stack, effects);
        } else {
            PotionUtil.setPotion(stack, DUBIOUS_POTION);
        }

        stack.putIfNotNull(EXTRA_DATA, extraNbt);

        return stack;
    }

    public boolean isEmpty() {
        return this == EMPTY || (basePotion == Potions.EMPTY && effects.isEmpty());
    }

    public int color() {
        return color;
    }

    public List<StatusEffectInstance> effects() {
        return effects;
    }

    public Potion basePotion() {
        return basePotion;
    }

    public NbtCompound extraNbt() {
        return extraNbt;
    }

    public NbtCompound getOrCreateExtraNbt() {
        if (extraNbt == null) {
            extraNbt = new NbtCompound();
        }

        return extraNbt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PotionMixture that = (PotionMixture) o;

        return pure == that.pure && color == that.color
                && Objects.equals(basePotion, that.basePotion)
                && Objects.equals(effects, that.effects)
                && Objects.equals(extraNbt, that.extraNbt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basePotion, effects, pure, color, extraNbt);
    }
}
