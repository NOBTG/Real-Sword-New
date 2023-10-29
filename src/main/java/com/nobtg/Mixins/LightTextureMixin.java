package com.nobtg.Mixins;

import com.mojang.blaze3d.platform.NativeImage;
import com.nobtg.Utils.GetterAndSetters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LightTexture.class, priority = 0x7fffffff)
public abstract class LightTextureMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private boolean updateLightTexture;

    @Shadow
    protected abstract float getDarknessGamma(float p_234320_);

    @Shadow
    protected abstract float calculateDarknessScale(LivingEntity p_234313_, float p_234314_, float p_234315_);

    @Shadow
    private float blockLightRedFlicker;

    @Shadow
    public static float getBrightness(DimensionType p_234317_, int p_234318_) {
        throw new RuntimeException();
    }

    @Shadow
    private static void clampColor(Vector3f p_254122_) {
        throw new RuntimeException();
    }

    @Shadow
    @Final
    private GameRenderer renderer;

    @Shadow
    protected abstract float notGamma(float p_109893_);

    @Shadow
    @Final
    private NativeImage lightPixels;

    @Shadow
    @Final
    private DynamicTexture lightTexture;

    @Inject(method = "updateLightTexture", at = @At("HEAD"), cancellable = true)
    private void updateLightTexture(float p_109882_, CallbackInfo ci) {
        if (this.updateLightTexture && GetterAndSetters.getRealEntities().contains(this.minecraft.player)) {
            this.updateLightTexture = false;
            this.minecraft.getProfiler().push("lightTex");
            ClientLevel clientlevel = this.minecraft.level;
            if (clientlevel != null) {
                float f = clientlevel.getSkyDarken(1.0F);
                float f1;
                if (clientlevel.getSkyFlashTime() > 0) f1 = 1.0F;
                else f1 = f * 0.95F + 0.05F;
                float f2 = this.minecraft.options.darknessEffectScale().get().floatValue();
                float f3 = this.getDarknessGamma(p_109882_) * f2;
                float f4 = this.calculateDarknessScale(this.minecraft.player, f3, p_109882_) * f2;
                float f5 = 1.0F;
                Vector3f vector3f = (new Vector3f(f, f, 1.0F)).lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
                float f7 = this.blockLightRedFlicker + 1.5F;
                Vector3f vector3f1 = new Vector3f();
                for (int i = 0; i < 16; ++i) {
                    for (int j = 0; j < 16; ++j) {
                        float f8 = getBrightness(clientlevel.dimensionType(), i) * f1;
                        float f9 = getBrightness(clientlevel.dimensionType(), j) * f7;
                        float f10 = f9 * ((f9 * 0.6F + 0.4F) * 0.6F + 0.4F);
                        float f11 = f9 * (f9 * f9 * 0.6F + 0.4F);
                        vector3f1.set(f9, f10, f11);
                        boolean flag = clientlevel.effects().forceBrightLightmap();
                        if (flag) {
                            vector3f1.lerp(new Vector3f(0.99F, 1.12F, 1.0F), 0.25F);
                            clampColor(vector3f1);
                        } else {
                            Vector3f vector3f2 = (new Vector3f(vector3f)).mul(f8);
                            vector3f1.add(vector3f2);
                            vector3f1.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                            if (this.renderer.getDarkenWorldAmount(p_109882_) > 0.0F) {
                                float f12 = this.renderer.getDarkenWorldAmount(p_109882_);
                                Vector3f vector3f3 = (new Vector3f(vector3f1)).mul(0.7F, 0.6F, 0.6F);
                                vector3f1.lerp(vector3f3, f12);
                            }
                        }
                        clientlevel.effects().adjustLightmapColors(clientlevel, p_109882_, f, f7, f8, j, i, vector3f1);
                        if (f5 > 0.0F) {
                            float f13 = Math.max(vector3f1.x(), Math.max(vector3f1.y(), vector3f1.z()));
                            if (f13 < 1.0F) {
                                float f15 = 1.0F / f13;
                                Vector3f vector3f5 = (new Vector3f(vector3f1)).mul(f15);
                                vector3f1.lerp(vector3f5, f5);
                            }
                        }
                        if (!flag) {
                            if (f4 > 0.0F)
                                vector3f1.add(-f4, -f4, -f4);
                            clampColor(vector3f1);
                        }
                        float f14 = this.minecraft.options.gamma().get().floatValue();
                        Vector3f vector3f4 = new Vector3f(this.notGamma(vector3f1.x), this.notGamma(vector3f1.y), this.notGamma(vector3f1.z));
                        vector3f1.lerp(vector3f4, Math.max(0.0F, f14 - f3));
                        vector3f1.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                        clampColor(vector3f1);
                        vector3f1.mul(255.0F);
                        int k = (int) vector3f1.x();
                        int l = (int) vector3f1.y();
                        int i1 = (int) vector3f1.z();
                        this.lightPixels.setPixelRGBA(j, i, -16777216 | i1 << 16 | l << 8 | k);
                    }
                }
                this.lightTexture.upload();
                this.minecraft.getProfiler().pop();
            }
            ci.cancel();
        }
    }
}
