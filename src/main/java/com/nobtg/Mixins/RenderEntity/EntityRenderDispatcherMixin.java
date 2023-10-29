package com.nobtg.Mixins.RenderEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nobtg.Utils.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderDispatcher.class, priority = 0x7fffffff)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(Entity p_114385_, double p_114386_, double p_114387_, double p_114388_, float p_114389_, float p_114390_, PoseStack p_114391_, MultiBufferSource p_114392_, int p_114393_, CallbackInfo ci) {
        if (Util.IsRealDead(p_114385_)) ci.cancel();
    }

    @Inject(method = "renderFlame", at = @At("HEAD"), cancellable = true)
    private void renderFlame(PoseStack p_114454_, MultiBufferSource p_114455_, Entity p_114456_, CallbackInfo ci) {
        if (Util.IsRealDead(p_114456_)) ci.cancel();
    }

    @Inject(method = "renderShadow", at = @At("HEAD"), cancellable = true)
    private static void renderShadow(PoseStack p_114458_, MultiBufferSource p_114459_, Entity p_114460_, float p_114461_, float p_114462_, LevelReader p_114463_, float p_114464_, CallbackInfo ci) {
        if (Util.IsRealDead(p_114460_)) ci.cancel();
    }

    @Inject(method = "renderHitbox", at = @At("HEAD"), cancellable = true)
    private static void renderHitbox(PoseStack p_114442_, VertexConsumer p_114443_, Entity p_114444_, float p_114445_, CallbackInfo ci) {
        if (Util.IsRealDead(p_114444_)) ci.cancel();
    }
}