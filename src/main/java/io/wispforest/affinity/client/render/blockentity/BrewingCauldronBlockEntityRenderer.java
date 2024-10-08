package io.wispforest.affinity.client.render.blockentity;

import io.wispforest.affinity.blockentity.impl.BrewingCauldronBlockEntity;
import io.wispforest.affinity.misc.util.MathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class BrewingCauldronBlockEntityRenderer implements BlockEntityRenderer<BrewingCauldronBlockEntity> {

    private static final SpriteIdentifier WATER_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("block/water_still"));
    private static final ModelPart POTION_MODEL;

    static {
        ModelData data = new ModelData();
        data.getRoot().addChild("potionFluid", ModelPartBuilder.create().uv(-24, 0).cuboid(0, 0, 0, 12, 0, 12), ModelTransform.NONE);
        POTION_MODEL = TexturedModelData.of(data, 16, 16).createModel();
    }

    public BrewingCauldronBlockEntityRenderer(BlockEntityRendererFactory.Context context) {}

    @Override
    public void render(BrewingCauldronBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.itemAvailable()) {
            matrices.push();
            matrices.translate(0.5, 0.6, 0.5);

            float angle = (float) ((System.currentTimeMillis() / 1000d) % (2 * Math.PI));

            matrices.scale(0.5f, 0.5f, 0.5f);

            for (int i = 0; i < entity.getItems().size(); i++) {
                final var stack = entity.getItems().get(i);
                if (stack.isEmpty()) continue;

                matrices.push();

                final var itemAngle = (float) (angle + i * 0.4 * Math.PI);
                matrices.translate(0.5 * MathHelper.cos(itemAngle), MathHelper.sin(itemAngle + angle) * 0.2, 0.5 * MathHelper.sin(itemAngle));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(itemAngle));

                MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers, 0);

                matrices.pop();
            }

            MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().draw();

            matrices.pop();
        }

        if (!entity.storedPotion().isEmpty()) {
            matrices.push();
            matrices.translate(0.125, entity.fluidHeight(), 0.125);

            VertexConsumer consumer = WATER_TEXTURE.getVertexConsumer(vertexConsumers, identifier -> RenderLayer.getTranslucent());
            float[] color = MathUtil.splitRGBToFloats(entity.storedPotion().color());

            POTION_MODEL.render(matrices, consumer, light, overlay, color[0], color[1], color[2], 1);

            matrices.pop();
        }
    }
}
