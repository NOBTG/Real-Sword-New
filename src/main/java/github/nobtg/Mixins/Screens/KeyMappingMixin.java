package github.nobtg.Mixins.Screens;

import github.nobtg.Utils.ClientUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = KeyMapping.class, priority = 0x7fffffff)
public abstract class KeyMappingMixin {
    @Shadow
    public static void setAll() {
    }

    @Inject(method = "releaseAll", at = @At("HEAD"), cancellable = true)
    private static void releaseAll(CallbackInfo ci) {
        if (ClientUtil.getNoDrawScreenVal(Minecraft.getInstance())) {
            setAll();
            ci.cancel();
        }
    }
}
