package io.wispforest.affinity.item;

import io.wispforest.affinity.block.impl.PeculiarClumpBlock;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.ops.WorldOps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;

public class GeologicalResonatorItem extends Item {

    public GeologicalResonatorItem() {
        super(AffinityItems.settings().maxCount(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        final var world = context.getWorld();
        final var pos = context.getBlockPos();

        if (world.getBlockState(pos).getBlock() != AffinityBlocks.PECULIAR_CLUMP) {
            return ActionResult.PASS;
        }

        if (world.isClient()) return ActionResult.SUCCESS;
        final var validDirection = PeculiarClumpBlock.getValidDirection(pos);
        final var side = context.getSide();
        WorldOps.playSound(world, pos, side == validDirection ? SoundEvents.BLOCK_AMETHYST_BLOCK_HIT : SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS);

        return ActionResult.SUCCESS;
    }
}
