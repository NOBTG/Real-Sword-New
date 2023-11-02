package github.nobtg.Mixins.TimeStop;

import github.nobtg.Utils.TimeStop.TimeStop;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = GameRenderer.class, priority = 0x7fffffff)
public abstract class GameRendererMixin {
    @ModifyVariable(method = "renderItemInHand", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public float partials(float val) {
        return TimeStop.getPartialTick(val);
    }
}
