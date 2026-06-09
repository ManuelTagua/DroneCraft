package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.entity.ScoutDroneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class ScoutDroneModel extends EntityModel<ScoutDroneEntity>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ExampleMod.MODID, "scout_drone"), "main");

    private final ModelPart body;
    private final ModelPart camera;
    private final ModelPart frontArm;
    private final ModelPart sideArm;
    private final ModelPart frontLeftRotor;
    private final ModelPart frontRightRotor;
    private final ModelPart rearLeftRotor;
    private final ModelPart rearRightRotor;

    public ScoutDroneModel(ModelPart root)
    {
        this.body = root.getChild("body");
        this.camera = root.getChild("camera");
        this.frontArm = root.getChild("front_arm");
        this.sideArm = root.getChild("side_arm");
        this.frontLeftRotor = root.getChild("front_left_rotor");
        this.frontRightRotor = root.getChild("front_right_rotor");
        this.rearLeftRotor = root.getChild("rear_left_rotor");
        this.rearRightRotor = root.getChild("rear_right_rotor");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -2.0F, -3.0F, 8.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, 22.0F, 0.0F));
        root.addOrReplaceChild("camera", CubeListBuilder.create()
                .texOffs(28, 0)
                .addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 21.5F, 4.0F));
        root.addOrReplaceChild("front_arm", CubeListBuilder.create()
                .texOffs(0, 12)
                .addBox(-0.75F, -0.65F, -10.0F, 1.5F, 1.3F, 20.0F),
                PartPose.offsetAndRotation(0.0F, 22.0F, 0.0F, 0.0F, 0.7853982F, 0.0F));
        root.addOrReplaceChild("side_arm", CubeListBuilder.create()
                .texOffs(0, 32)
                .addBox(-0.75F, -0.65F, -10.0F, 1.5F, 1.3F, 20.0F),
                PartPose.offsetAndRotation(0.0F, 22.0F, 0.0F, 0.0F, -0.7853982F, 0.0F));

        addRotor(root, "front_left_rotor", -6.75F, 21.1F, -6.75F);
        addRotor(root, "front_right_rotor", 6.75F, 21.1F, -6.75F);
        addRotor(root, "rear_left_rotor", -6.75F, 21.1F, 6.75F);
        addRotor(root, "rear_right_rotor", 6.75F, 21.1F, 6.75F);

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    private static void addRotor(PartDefinition root, String name, float x, float y, float z)
    {
        PartDefinition rotor = root.addOrReplaceChild(name, CubeListBuilder.create()
                .texOffs(42, 0)
                .addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F)
                .texOffs(50, 0)
                .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.4F, 1.0F),
                PartPose.offset(x, y, z));
        rotor.addOrReplaceChild("blade_x", CubeListBuilder.create()
                .texOffs(42, 6)
                .addBox(-4.0F, -0.25F, -0.5F, 8.0F, 0.5F, 1.0F),
                PartPose.ZERO);
        rotor.addOrReplaceChild("blade_z", CubeListBuilder.create()
                .texOffs(42, 10)
                .addBox(-0.5F, -0.25F, -4.0F, 1.0F, 0.5F, 8.0F),
                PartPose.ZERO);
    }

    @Override
    public void setupAnim(ScoutDroneEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        float spin = entity.getRotorAngle(ageInTicks);
        frontLeftRotor.yRot = spin;
        rearRightRotor.yRot = spin;
        frontRightRotor.yRot = -spin;
        rearLeftRotor.yRot = -spin;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        renderPart(body, poseStack, vertexConsumer, packedLight, packedOverlay, 0.08F, 0.09F, 0.10F, alpha);
        renderPart(frontArm, poseStack, vertexConsumer, packedLight, packedOverlay, 0.18F, 0.19F, 0.20F, alpha);
        renderPart(sideArm, poseStack, vertexConsumer, packedLight, packedOverlay, 0.18F, 0.19F, 0.20F, alpha);
        renderPart(camera, poseStack, vertexConsumer, packedLight, packedOverlay, 0.08F, 0.28F, 0.45F, alpha);
        renderPart(frontLeftRotor, poseStack, vertexConsumer, packedLight, packedOverlay, 0.03F, 0.03F, 0.035F, alpha);
        renderPart(frontRightRotor, poseStack, vertexConsumer, packedLight, packedOverlay, 0.03F, 0.03F, 0.035F, alpha);
        renderPart(rearLeftRotor, poseStack, vertexConsumer, packedLight, packedOverlay, 0.03F, 0.03F, 0.035F, alpha);
        renderPart(rearRightRotor, poseStack, vertexConsumer, packedLight, packedOverlay, 0.03F, 0.03F, 0.035F, alpha);
    }

    private static void renderPart(ModelPart part, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        part.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
