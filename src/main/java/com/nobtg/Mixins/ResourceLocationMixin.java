package com.nobtg.Mixins;

import com.nobtg.Utils.GetterAndSetters;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ResourceLocation.class, priority = 0x7fffffff)
public abstract class ResourceLocationMixin {
    @Inject(method = "decompose", at = @At("RETURN"), cancellable = true)
    private static void decompose(String p_135833_, char p_135834_, CallbackInfoReturnable<String[]> cir) {
        if (GetterAndSetters.getAllUnRealEntities().contains(p_135833_)) cir.setReturnValue(new String[]{cir.getReturnValue()[0], "null"});
    }
}