package com.medievalfactions.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

/** Applies the selected local cosmetic to the local player's third-person model. */
public final class MFPlayerSkinLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public MFPlayerSkinLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.isInvisible()) return;

        ResourceLocation texture = MFPlayerSkinClient.skinFor(player);
        if (texture == null) return;
        poseStack.pushPose();
        // A tiny expansion avoids depth fighting with the vanilla player texture rendered below.
        poseStack.scale(1.001F, 1.001F, 1.001F);
        getParentModel().renderToBuffer(
                poseStack,
                buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),
                packedLight,
                LivingEntityRenderer.getOverlayCoords(player, 0.0F));
        poseStack.popPose();
    }
}
