package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.ops.ItemOps;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class AethumFluxBottleItem extends Item implements DirectInteractionHandler {

    private final List<Block> INTERACTION_OVERRIDES = List.of(Blocks.FLOWERING_AZALEA_LEAVES);

    public AethumFluxBottleItem() {
        super(AffinityItems.settings(AffinityItemGroup.MAIN).maxCount(7));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        final var world = context.getWorld();
        final var pos = context.getBlockPos();
        final var state = world.getBlockState(pos);

        if (state.isOf(Blocks.FLOWERING_AZALEA_LEAVES)) {
            world.setBlockState(pos, AffinityBlocks.UNFLOWERING_AZALEA_LEAVES.getDefaultState());
            this.useStack(context);

            return ActionResult.SUCCESS;
        }

        var member = Affinity.AETHUM_MEMBER.find(world, pos, null);
        if (member == null) return ActionResult.PASS;
        if (world.isClient) return ActionResult.SUCCESS;

        try (var transaction = Transaction.openOuter()) {
            if (member.insert(1000, transaction) != 1000) return ActionResult.PASS;

            this.useStack(context);
            transaction.commit();
        }

        return ActionResult.SUCCESS;
    }

    private void useStack(ItemUsageContext context) {
        final var player = context.getPlayer();
        if (player != null) {
            ItemOps.decrementPlayerHandItem(player, context.getHand());
            if (!player.isCreative()) player.getInventory().offerOrDrop(Items.GLASS_BOTTLE.getDefaultStack());
        }
    }

    @Override
    public Collection<Block> interactionOverrideCandidates() {
        return INTERACTION_OVERRIDES;
    }
}
