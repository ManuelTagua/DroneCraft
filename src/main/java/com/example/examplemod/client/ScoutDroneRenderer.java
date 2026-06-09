package com.example.examplemod.client;

import com.example.examplemod.entity.ScoutDroneEntity;
import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ScoutDroneRenderer extends EntityRenderer<ScoutDroneEntity>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/block/white_concrete.png");

    private final ScoutDroneModel model;

    public ScoutDroneRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        this.model = new ScoutDroneModel(context.bakeLayer(ScoutDroneModel.LAYER_LOCATION));
    }

    @Override
    public void render(ScoutDroneEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
    {
        if (DroneClientPilot.isPilotingDrone(entity)) {
            return;
        }

        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        poseStack.pushPose();
        poseStack.translate(0.0F, 1.5F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        poseStack.scale(1.0F, -1.0F, -1.0F);
        model.setupAnim(entity, 0.0F, 0.0F, partialTick, 0.0F, 0.0F);
        model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity))), packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ScoutDroneEntity entity)
    {
        return TEXTURE;
    }
}
