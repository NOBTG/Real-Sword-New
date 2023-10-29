package com.nobtg.Mixins.SwordBlock;

import com.nobtg.Utils.Util;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HumanoidModel.class, priority = 0x7fffffff)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel {
    @Final
    @Shadow
    public ModelPart rightArm;
    @Final
    @Shadow
    public ModelPart leftArm;

    @Shadow
    @Final
    public ModelPart head;

    @Shadow
    public float swimAmount;

    @Shadow
    protected abstract float rotlerpRad(float p_102836_, float p_102837_, float p_102838_);

    @Shadow
    @Final
    public ModelPart rightLeg;

    @Shadow
    @Final
    public ModelPart leftLeg;

    @Shadow
    protected abstract void poseRightArm(T p_102876_);

    @Shadow
    protected abstract void poseLeftArm(T p_102879_);

    @Shadow
    @Final
    public ModelPart body;

    @Shadow
    protected abstract float quadraticArmUpdate(float p_102834_);

    @Shadow
    protected abstract HumanoidArm getAttackArm(T p_102857_);

    @Shadow
    protected abstract void setupAttackAnimation(T p_102858_, float p_102859_);

    @Shadow
    public HumanoidModel.ArmPose leftArmPose;

    @Shadow
    public HumanoidModel.ArmPose rightArmPose;

    @Shadow
    @Final
    public ModelPart hat;

    @Shadow
    public boolean crouching;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("HEAD"), cancellable = true)
    private void setupAnim(T p_102866_, float p_102867_, float p_102868_, float p_102869_, float p_102870_, float p_102871_, CallbackInfo ci) {
        boolean flag = p_102866_.getFallFlyingTicks() > 4;
        boolean flag1 = p_102866_.isVisuallySwimming();
        this.head.yRot = p_102870_ * ((float) Math.PI / 180F);
        if (flag) this.head.xRot = (-(float) Math.PI / 4F);
        else if (this.swimAmount > 0.0F) {
            if (flag1) this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, (-(float) Math.PI / 4F));
            else
                this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, p_102871_ * ((float) Math.PI / 180F));
        } else this.head.xRot = p_102871_ * ((float) Math.PI / 180F);
        this.body.yRot = 0.0F;
        this.rightArm.z = 0.0F;
        this.rightArm.x = -5.0F;
        this.leftArm.z = 0.0F;
        this.leftArm.x = 5.0F;
        float f = 1.0F;
        if (flag) {
            f = (float) p_102866_.getDeltaMovement().lengthSqr();
            f /= 0.2F;
            f *= f * f;
        }
        if (f < 1.0F) f = 1.0F;
        this.rightArm.xRot = Mth.cos(p_102867_ * 0.6662F + (float) Math.PI) * 2.0F * p_102868_ * 0.5F / f;
        this.leftArm.xRot = Mth.cos(p_102867_ * 0.6662F) * 2.0F * p_102868_ * 0.5F / f;
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightLeg.xRot = Mth.cos(p_102867_ * 0.6662F) * 1.4F * p_102868_ / f;
        this.leftLeg.xRot = Mth.cos(p_102867_ * 0.6662F + (float) Math.PI) * 1.4F * p_102868_ / f;
        this.rightLeg.yRot = 0.005F;
        this.leftLeg.yRot = -0.005F;
        this.rightLeg.zRot = 0.005F;
        this.leftLeg.zRot = -0.005F;
        if (this.riding) {
            this.rightArm.xRot += (-(float) Math.PI / 5F);
            this.leftArm.xRot += (-(float) Math.PI / 5F);
            this.rightLeg.xRot = -1.4137167F;
            this.rightLeg.yRot = ((float) Math.PI / 10F);
            this.rightLeg.zRot = 0.07853982F;
            this.leftLeg.xRot = -1.4137167F;
            this.leftLeg.yRot = (-(float) Math.PI / 10F);
            this.leftLeg.zRot = -0.07853982F;
        }
        this.rightArm.yRot = 0.0F;
        this.leftArm.yRot = 0.0F;
        boolean flag2 = p_102866_.getMainArm() == HumanoidArm.RIGHT;
        if (p_102866_.isUsingItem()) {
            boolean flag3 = p_102866_.getUsedItemHand() == InteractionHand.MAIN_HAND;
            if (flag3 == flag2) this.poseRightArm(p_102866_);
            else this.poseLeftArm(p_102866_);
        } else {
            boolean flag4 = flag2 ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
            if (flag2 != flag4) {
                this.poseLeftArm(p_102866_);
                this.poseRightArm(p_102866_);
            } else {
                this.poseRightArm(p_102866_);
                this.poseLeftArm(p_102866_);
            }
        }
        // Start Modify
        if (p_102866_ instanceof Player player && Util.isBlocking(player)) {
            float v = (3.14159265358979323846F * 2.0F) / 10.0F;
            float o = this.leftArm.xRot;
            float m = this.rightArm.xRot;
            InteractionHand h = player.getUsedItemHand();
            this.leftArm.xRot = h == InteractionHand.OFF_HAND ? o - v : o;
            this.rightArm.xRot = h == InteractionHand.MAIN_HAND ? m - v : m;
        }
        // End Modify
        this.setupAttackAnimation(p_102866_, p_102869_);
        if (this.crouching) {
            this.body.xRot = 0.5F;
            this.rightArm.xRot += 0.4F;
            this.leftArm.xRot += 0.4F;
            this.rightLeg.z = 4.0F;
            this.leftLeg.z = 4.0F;
            this.rightLeg.y = 12.2F;
            this.leftLeg.y = 12.2F;
            this.head.y = 4.2F;
            this.body.y = 3.2F;
            this.leftArm.y = 5.2F;
            this.rightArm.y = 5.2F;
        } else {
            this.body.xRot = 0.0F;
            this.rightLeg.z = 0.0F;
            this.leftLeg.z = 0.0F;
            this.rightLeg.y = 12.0F;
            this.leftLeg.y = 12.0F;
            this.head.y = 0.0F;
            this.body.y = 0.0F;
            this.leftArm.y = 2.0F;
            this.rightArm.y = 2.0F;
        }
        if (this.rightArmPose != HumanoidModel.ArmPose.SPYGLASS)
            AnimationUtils.bobModelPart(this.rightArm, p_102869_, 1.0F);
        if (this.leftArmPose != HumanoidModel.ArmPose.SPYGLASS)
            AnimationUtils.bobModelPart(this.leftArm, p_102869_, -1.0F);
        if (this.swimAmount > 0.0F) {
            float f5 = p_102867_ % 26.0F;
            HumanoidArm humanoidarm = this.getAttackArm(p_102866_);
            float f1 = humanoidarm == HumanoidArm.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
            float f2 = humanoidarm == HumanoidArm.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
            if (!p_102866_.isUsingItem()) {
                if (f5 < 14.0F) {
                    this.leftArm.xRot = this.rotlerpRad(f2, this.leftArm.xRot, 0.0F);
                    this.rightArm.xRot = Mth.lerp(f1, this.rightArm.xRot, 0.0F);
                    this.leftArm.yRot = this.rotlerpRad(f2, this.leftArm.yRot, (float) Math.PI);
                    this.rightArm.yRot = Mth.lerp(f1, this.rightArm.yRot, (float) Math.PI);
                    this.leftArm.zRot = this.rotlerpRad(f2, this.leftArm.zRot, (float) Math.PI + 1.8707964F * this.quadraticArmUpdate(f5) / this.quadraticArmUpdate(14.0F));
                    this.rightArm.zRot = Mth.lerp(f1, this.rightArm.zRot, (float) Math.PI - 1.8707964F * this.quadraticArmUpdate(f5) / this.quadraticArmUpdate(14.0F));
                } else if (f5 >= 14.0F && f5 < 22.0F) {
                    float f6 = (f5 - 14.0F) / 8.0F;
                    this.leftArm.xRot = this.rotlerpRad(f2, this.leftArm.xRot, ((float) Math.PI / 2F) * f6);
                    this.rightArm.xRot = Mth.lerp(f1, this.rightArm.xRot, ((float) Math.PI / 2F) * f6);
                    this.leftArm.yRot = this.rotlerpRad(f2, this.leftArm.yRot, (float) Math.PI);
                    this.rightArm.yRot = Mth.lerp(f1, this.rightArm.yRot, (float) Math.PI);
                    this.leftArm.zRot = this.rotlerpRad(f2, this.leftArm.zRot, 5.012389F - 1.8707964F * f6);
                    this.rightArm.zRot = Mth.lerp(f1, this.rightArm.zRot, 1.2707963F + 1.8707964F * f6);
                } else if (f5 >= 22.0F && f5 < 26.0F) {
                    float f3 = (f5 - 22.0F) / 4.0F;
                    this.leftArm.xRot = this.rotlerpRad(f2, this.leftArm.xRot, ((float) Math.PI / 2F) - ((float) Math.PI / 2F) * f3);
                    this.rightArm.xRot = Mth.lerp(f1, this.rightArm.xRot, ((float) Math.PI / 2F) - ((float) Math.PI / 2F) * f3);
                    this.leftArm.yRot = this.rotlerpRad(f2, this.leftArm.yRot, (float) Math.PI);
                    this.rightArm.yRot = Mth.lerp(f1, this.rightArm.yRot, (float) Math.PI);
                    this.leftArm.zRot = this.rotlerpRad(f2, this.leftArm.zRot, (float) Math.PI);
                    this.rightArm.zRot = Mth.lerp(f1, this.rightArm.zRot, (float) Math.PI);
                }
            }
            this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * Mth.cos(p_102867_ * 0.33333334F + (float) Math.PI));
            this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * Mth.cos(p_102867_ * 0.33333334F));
        }
        this.hat.copyFrom(this.head);
        ci.cancel();
    }
}