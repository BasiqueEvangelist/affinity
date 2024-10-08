package io.wispforest.affinity.misc.screenhandler;

import io.wispforest.affinity.blockentity.impl.AssemblyAugmentBlockEntity;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.mixin.access.CraftingInventoryAccessor;
import io.wispforest.affinity.mixin.access.CraftingScreenHandlerAccessor;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.client.screens.SyncedProperty;
import io.wispforest.owo.client.screens.ValidatingSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import org.jetbrains.annotations.Nullable;

public class AssemblyAugmentScreenHandler extends CraftingScreenHandler {

    private final @Nullable AssemblyAugmentBlockEntity augment;
    private final SyncedProperty<Integer> displayTreetaps;
    private final SyncedProperty<Float> craftingProgress;

    public static AssemblyAugmentScreenHandler client(int syncId, PlayerInventory inventory) {
        MixinHooks.INJECT_ASSEMBLY_AUGMENT_SCREEN = true;
        return new AssemblyAugmentScreenHandler(syncId, inventory, null);
    }

    public static AssemblyAugmentScreenHandler server(int syncId, PlayerInventory inventory, AssemblyAugmentBlockEntity augment) {
        MixinHooks.INJECT_ASSEMBLY_AUGMENT_SCREEN = true;
        return new AssemblyAugmentScreenHandler(syncId, inventory, augment);
    }

    private AssemblyAugmentScreenHandler(int syncId, PlayerInventory inventory, @Nullable AssemblyAugmentBlockEntity augment) {
        super(syncId, inventory, augment == null ? ScreenHandlerContext.EMPTY : ScreenHandlerContext.create(augment.getWorld(), augment.getPos()));
        this.augment = augment;

        this.displayTreetaps = this.createProperty(int.class, 0);
        this.craftingProgress = this.createProperty(float.class, 0f);

        this.addSlot(new ValidatingSlot(this.augment != null ? this.augment.outputInventory() : new SimpleInventory(1), 0, this.getSlot(0).x, this.getSlot(0).y, stack -> false));

        if (this.augment != null) {
            ((CraftingInventoryAccessor) ((CraftingScreenHandlerAccessor) this).affinity$getInput()).affinity$setStacks(
                    this.augment.craftingInput().stacks
            );
        }

        this.onContentChanged(((CraftingScreenHandlerAccessor) this).affinity$getInput());
    }

    @Override
    protected void dropInventory(PlayerEntity player, Inventory inventory) {}

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        if (this.augment != null) this.augment.markDirty();
    }

    @Override
    public void sendContentUpdates() {
        if (this.augment != null) {
            this.craftingProgress.set(this.augment.craftingTick() / (float) this.augment.craftingDuration());
            this.displayTreetaps.set(this.augment.displayTreetaps());
        }

        super.sendContentUpdates();
    }

    public int treetapCount() {
        return this.displayTreetaps.get();
    }

    public float craftingProgress() {
        return this.craftingProgress.get();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        if (this.augment == null) return false;
        return this.augment.getWorld().getBlockState(this.augment.getPos()).isOf(AffinityBlocks.ASSEMBLY_AUGMENT);
    }
}
