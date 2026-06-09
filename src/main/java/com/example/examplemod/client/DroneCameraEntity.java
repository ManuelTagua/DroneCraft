package com.example.examplemod.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DroneCameraEntity extends Entity
{
    private static final double SNAP_DISTANCE_SQR = 2.25D;
    private static final double POSITION_LERP_FACTOR = 0.55D;

    public DroneCameraEntity(Level level)
    {
        super(EntityType.MARKER, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public void snapToDrone(Vec3 targetPos, float yaw, float pitch)
    {
        setPos(targetPos);
        xo = targetPos.x;
        yo = targetPos.y;
        zo = targetPos.z;
        setCameraRotation(yaw, pitch);
        yRotO = getYRot();
        xRotO = getXRot();
    }

    public void syncVisualPose(Vec3 targetPos, float yaw, float pitch)
    {
        xo = getX();
        yo = getY();
        zo = getZ();

        Vec3 currentPos = position();
        Vec3 nextPos = currentPos.distanceToSqr(targetPos) > SNAP_DISTANCE_SQR
                ? targetPos
                : currentPos.lerp(targetPos, POSITION_LERP_FACTOR);
        setPos(nextPos);
        setCameraRotation(yaw, pitch);
    }

    private void setCameraRotation(float yaw, float pitch)
    {
        float clampedPitch = Mth.clamp(pitch, -89.0F, 89.0F);
        setYRot(Mth.wrapDegrees(yaw));
        setXRot(clampedPitch);
        yRotO = getYRot();
        xRotO = getXRot();
        setYHeadRot(getYRot());
        setYBodyRot(getYRot());
    }

    @Override
    protected void defineSynchedData()
    {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag)
    {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag)
    {
    }

    @Override
    public boolean isPickable()
    {
        return false;
    }

    @Override
    public boolean shouldRender(double x, double y, double z)
    {
        return false;
    }

    @Override
    protected float getEyeHeight(Pose pose, EntityDimensions dimensions)
    {
        return 0.0F;
    }
}
