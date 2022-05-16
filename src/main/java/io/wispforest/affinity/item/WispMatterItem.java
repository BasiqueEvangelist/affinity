package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.util.BlockFinder;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.wisps.WispType;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WispMatterItem extends Item {

    private static final TranslatableText TRANSLATED_NAME = new TranslatableText(Util.createTranslationKey("item", Affinity.id("wisp_matter")));
    private final WispType type;

    public WispMatterItem(WispType type) {
        super(AffinityItems.settings(AffinityItemGroup.MAIN));
        this.type = type;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        final var world = context.getWorld();
        if (!world.getBlockState(context.getBlockPos()).isOf(AffinityBlocks.AZALEA_LOG)) return ActionResult.PASS;
        if (world.isClient) return ActionResult.SUCCESS;

        final var findResults = BlockFinder.findCapped(world, context.getBlockPos(), (blockPos, state) -> {
            if (state.isOf(Blocks.AZALEA_LEAVES) || state.isOf(Blocks.FLOWERING_AZALEA_LEAVES)) {
                return !state.get(LeavesBlock.PERSISTENT);
            }

            return state.isOf(AffinityBlocks.AZALEA_LOG);
        }, 128);
        final var result = findResults.byCount();

        final var player = context.getPlayer();
        final int logCount = result.getOrDefault(AffinityBlocks.AZALEA_LOG, 0);
        final int leavesCount = result.getOrDefault(Blocks.AZALEA_LEAVES, 0) + result.getOrDefault(Blocks.FLOWERING_AZALEA_LEAVES, 0);

        if (logCount > 5 && leavesCount > 40) {
            player.sendMessage(new LiteralText("yep, that is in fact a tree").formatted(Formatting.GREEN), false);
        } else {
            player.sendMessage(new LiteralText("nope, not a tree. no.").formatted(Formatting.RED), false);
        }

        if (this == AffinityItems.VICIOUS_WISP_MATTER) {
            for (var pos : findResults.results().keySet()) {
                world.breakBlock(pos, true);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public Text getName() {
        return TRANSLATED_NAME;
    }

    @Override
    public Text getName(ItemStack stack) {
        return TRANSLATED_NAME;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(TextOps.withColor(type.icon(), type.color()).append(" ").append(new TranslatableText(type.translationKey()).formatted(Formatting.GRAY)));
    }

    public WispType wispType() {
        return type;
    }
}
