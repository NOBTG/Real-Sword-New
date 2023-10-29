package com.nobtg.Mixins.Entity;

import com.nobtg.Utils.EntityUtil;
import com.nobtg.Utils.Util;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class, priority = 0x7fffffff)
public abstract class EntityMixin {
    @Inject(method = "onAddedToWorld", at = @At("HEAD"), cancellable = true, remap = false)
    private void onAddedToWorld(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (Util.IsRealDead(entity)) {
            EntityUtil.Remove_Entity(entity, false);
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (Util.IsRealDead(entity))
            EntityUtil.Remove_Entity(entity, false);
    }
}
