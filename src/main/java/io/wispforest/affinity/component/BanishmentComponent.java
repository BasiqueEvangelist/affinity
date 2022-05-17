package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import io.wispforest.affinity.item.EchoShardItem;
import io.wispforest.owo.util.NbtKey;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class BanishmentComponent implements Component {

    public static final NbtKey<Identifier> DIMENSION = new NbtKey<>("Dimension", EchoShardItem.IDENTIFIER_TYPE);
    public static final NbtKey<BlockPos> POSITION = new NbtKey<>("Pos", EchoShardItem.BLOCK_POS_TYPE);

    public Identifier dimension = World.OVERWORLD.getValue();
    public BlockPos pos = BlockPos.ORIGIN;

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        dimension = DIMENSION.get(tag);
        pos = POSITION.get(tag);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        DIMENSION.put(tag, this.dimension);
        POSITION.put(tag, this.pos);
    }
}
