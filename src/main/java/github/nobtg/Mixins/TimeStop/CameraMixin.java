package github.nobtg.Mixins.TimeStop;

import github.nobtg.Utils.TimeStop.TimeStop;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = Camera.class, priority = 0x7fffffff)
public abstract class CameraMixin {
    @ModifyVariable(method = "setup", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public float render(float val) {
        return TimeStop.getPartialTick(val);
    }
}