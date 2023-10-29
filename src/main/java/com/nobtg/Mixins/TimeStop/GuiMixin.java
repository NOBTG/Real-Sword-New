package com.nobtg.Mixins.TimeStop;

import com.nobtg.Utils.TimeStop.TimeStop;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float render(float val) {
        return TimeStop.getPartialTick(val);
    }
}
