package github.nobtg.Mixins.Screens;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import github.nobtg.RealSwordMod;
import github.nobtg.Utils.ClientUtil;
import github.nobtg.Utils.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RenderSystem.class, priority = 0x7fffffff)
public abstract class RenderSystemMixin {
    @SuppressWarnings("unchecked")
    @Inject(method = "_setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V", at = @At("HEAD"))
    private static void _setShaderTexture(int p_157180_, ResourceLocation p_157181_, CallbackInfo ci) {
        if (((Map<ResourceLocation, AbstractTexture>) Util.getField(Minecraft.getInstance().textureManager, TextureManager.class, "byPath", "f_118468_")).get(new ResourceLocation(RealSwordMod.MOD_ID, "nonnull")) == null) {
            DynamicTexture texture = new DynamicTexture(1, 1, false);
            NativeImage image = texture.getPixels();
            if (image != null) {
                image.setPixelRGBA(0, 0, 0);
                image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), false, true, false, false);
                texture.setPixels(image);
            }
            Minecraft.getInstance().textureManager.register(new ResourceLocation(RealSwordMod.MOD_ID, "nonnull"), texture);
        }
        if (ClientUtil.getNoDrawScreenVal(Minecraft.getInstance()) && Util.isResourceLocationMod(p_157181_)) p_157181_ = new ResourceLocation(RealSwordMod.MOD_ID, "nonnull");
    }


    @Inject(method = "_setShaderColor", at = @At("HEAD"), cancellable = true)
    private static void _setShaderColor(float p_157430_, float p_157431_, float p_157432_, float p_157433_, CallbackInfo ci) {
        if (ClientUtil.getNoDrawScreenVal(Minecraft.getInstance())) ci.cancel();
    }
}