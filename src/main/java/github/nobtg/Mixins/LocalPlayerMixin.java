package github.nobtg.Mixins;

import github.nobtg.Utils.ClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LocalPlayer.class, priority = 0x7fffffff)
public abstract class LocalPlayerMixin {
    @Shadow @Final protected Minecraft minecraft;

    @Inject(method = "shouldShowDeathScreen", at = @At("RETURN"), cancellable = true)
    private void shouldShowDeathScreen(CallbackInfoReturnable<Boolean> cir) {
        if (ClientUtil.isAttack(this.minecraft)) cir.setReturnValue(false);
    }
}
