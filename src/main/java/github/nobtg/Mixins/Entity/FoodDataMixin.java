package github.nobtg.Mixins.Entity;

import github.nobtg.Utils.EntityUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FoodData.class, priority = 0x7fffffff)
public abstract class FoodDataMixin {
    @Shadow
    private int foodLevel;
    @Shadow
    private int lastFoodLevel;
    @Shadow
    private float exhaustionLevel;
    @Shadow
    private float saturationLevel;
    @Unique
    private Player nobtg$player;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(Player p_38711_, CallbackInfo ci) {
        this.nobtg$player = p_38711_;
        if (EntityUtil.Check(nobtg$player)) {
            this.foodLevel = 20;
            this.lastFoodLevel = 20;
            this.saturationLevel = 20.0F;
            this.exhaustionLevel = 0.0F;
        }
    }

    @Inject(method = "getFoodLevel", at = @At("RETURN"), cancellable = true)
    private void getFoodLevel(CallbackInfoReturnable<Integer> cir) {
        if (EntityUtil.Check(nobtg$player)) cir.setReturnValue(20);
    }

    @Inject(method = "getLastFoodLevel", at = @At("RETURN"), cancellable = true)
    private void getLastFoodLevel(CallbackInfoReturnable<Integer> cir) {
        if (EntityUtil.Check(nobtg$player)) cir.setReturnValue(20);
    }

    @Inject(method = "getSaturationLevel", at = @At("RETURN"), cancellable = true)
    private void getSaturationLevel(CallbackInfoReturnable<Float> cir) {
        if (EntityUtil.Check(nobtg$player)) cir.setReturnValue(20.0F);
    }

    @Inject(method = "getExhaustionLevel", at = @At("RETURN"), cancellable = true)
    private void getExhaustionLevel(CallbackInfoReturnable<Float> cir) {
        if (EntityUtil.Check(nobtg$player)) cir.setReturnValue(0.0F);
    }

    @Inject(method = "setFoodLevel", at = @At("RETURN"), cancellable = true)
    private void setFoodLevel(int p_38706_, CallbackInfo ci) {
        if (EntityUtil.Check(nobtg$player)) {
            this.foodLevel = 20;
            ci.cancel();
        }
    }

    @Inject(method = "setSaturation", at = @At("RETURN"), cancellable = true)
    private void setSaturation(float p_38718_, CallbackInfo ci) {
        if (EntityUtil.Check(nobtg$player)) {
            this.saturationLevel = 20.0F;
            ci.cancel();
        }
    }

    @Inject(method = "setExhaustion", at = @At("RETURN"), cancellable = true)
    private void setExhaustion(float p_150379_, CallbackInfo ci) {
        if (EntityUtil.Check(nobtg$player)) {
            this.exhaustionLevel = 0.0F;
            ci.cancel();
        }
    }

    @Inject(method = "needsFood", at = @At("RETURN"), cancellable = true)
    private void needsFood(CallbackInfoReturnable<Boolean> cir) {
        if (EntityUtil.Check(nobtg$player)) cir.setReturnValue(false);
    }

    @Inject(method = "addExhaustion", at = @At("RETURN"), cancellable = true)
    private void addExhaustion(float p_38704_, CallbackInfo ci) {
        if (EntityUtil.Check(nobtg$player)) {
            this.exhaustionLevel = 0.0F;
            ci.cancel();
        }
    }
}
