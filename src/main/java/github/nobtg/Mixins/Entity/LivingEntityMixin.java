package github.nobtg.Mixins.Entity;

import github.nobtg.Utils.EntityUtil;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 0x7fffffff)
public abstract class LivingEntityMixin {
    @Inject(method = "getHealth", at = @At("RETURN"), cancellable = true)
    private void getHealth(@NotNull CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(EntityUtil.getHealth((LivingEntity) (Object) this, cir.getReturnValue()));
    }
}
