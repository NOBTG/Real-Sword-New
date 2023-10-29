package com.nobtg.Mixins.TimeStop;

import com.nobtg.Utils.TimeStop.TimeStop;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Util.class, priority = 0x7fffffff)
public abstract class UtilMixin {
    @Inject(method = "getMillis", at = @At("RETURN"), cancellable = true)
    private static void getMillis(@NotNull CallbackInfoReturnable<Long> cir) {
        cir.setReturnValue(TimeStop.get() ? 0L : cir.getReturnValue());
    }
}
