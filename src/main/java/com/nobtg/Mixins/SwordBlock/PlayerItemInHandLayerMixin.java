package com.nobtg.Mixins.SwordBlock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nobtg.Utils.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerItemInHandLayer.class, priority = 0x7fffffff)
public abstract class PlayerItemInHandLayerMixin<T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> {
    @Shadow
    @Final
    private ItemInHandRenderer itemInHandRenderer;

    @SuppressWarnings("unchecked")
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void renderArmWithItem(LivingEntity entity, ItemStack item, ItemDisplayContext c, HumanoidArm arm, PoseStack stack, MultiBufferSource source, int combinedLight, CallbackInfo ci) {
        if (entity instanceof Player player && ((!item.isEmpty() && item.getUseAnimation() == Util.getUseAnim() && entity.isUsingItem() && entity.getUseItem() == item) || Util.isBlocking(player))) {
            stack.pushPose();
            ((ItemInHandLayer<T, M>) (Object) this).getParentModel().translateToHand(arm, stack);
            boolean leftHand = arm == HumanoidArm.LEFT;
            stack.translate((leftHand ? 1.0F : -1.0F) / 16.0F, 0.4375F, 0.0625F);
            stack.translate(leftHand ? -0.035F : 0.05F, leftHand ? 0.045F : 0.0F, leftHand ? -0.135F : -0.1F);
            stack.mulPose(Axis.YP.rotationDegrees((leftHand ? -1.0F : 1.0F) * -50.0F));
            stack.mulPose(Axis.XP.rotationDegrees(-10.0F));
            stack.mulPose(Axis.ZP.rotationDegrees((leftHand ? -1.0F : 1.0F) * -60.0F));
            stack.translate(0.0F, 0.1875F, 0.0F);
            stack.scale(0.625F, 0.625F, 0.625F);
            stack.mulPose(Axis.XP.rotationDegrees(180.0F));
            stack.mulPose(Axis.XN.rotationDegrees(-100.0F));
            stack.mulPose(Axis.YN.rotationDegrees(leftHand ? 35.0F : 45.0F));
            stack.translate(0.0F, -0.3F, 0.0F);
            stack.scale(1.5F, 1.5F, 1.5F);
            stack.mulPose(Axis.YN.rotationDegrees(50.0F));
            stack.mulPose(Axis.ZP.rotationDegrees(335.0F));
            stack.translate(-0.9375F, -0.0625F, 0.0F);
            stack.translate(0.5F, 0.5F, 0.25F);
            stack.mulPose(Axis.YN.rotationDegrees(180.0F));
            stack.translate(0.0F, 0.0F, 0.28125F);
            ItemTransform vec = Minecraft.getInstance().getItemRenderer().getModel(item, entity.level(), entity, 0).getTransforms().getTransform(c);
            if (vec != ItemTransform.NO_TRANSFORM) {
                Quaternionf quaternion = new Quaternionf().rotationXYZ((vec.rotation.x()) * 0.017453292F, (leftHand ? -vec.rotation.y() : vec.rotation.y()) * 0.017453292F, (leftHand ? -vec.rotation.z() : vec.rotation.z()) * 0.017453292F);
                quaternion.conjugate();
                stack.scale(1.0F / vec.scale.x(), 1.0F / vec.scale.y(), 1.0F / vec.scale.z());
                stack.mulPose(quaternion);
                stack.translate((leftHand ? -1.0F : 1.0F) * -vec.translation.x(), -vec.translation.y(), -vec.translation.z());
            }
            this.itemInHandRenderer.renderItem(entity, item, c, leftHand, stack, source, combinedLight);
            stack.popPose();
            ci.cancel();
        }
    }
}