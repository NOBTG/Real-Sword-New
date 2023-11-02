package github.nobtg.Mixins.Screens;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ResourceLocation.class, priority = 0x7fffffff)
public abstract class ResourceLocationMixin {
    @Unique
    public String nobtg$val = "";

    @Inject(method = "<init>(Ljava/lang/String;)V", at = @At("RETURN"))
    private void init(String p_135809_, CallbackInfo ci) {
        this.nobtg$val = p_135809_;
    }

    @Inject(method = "<init>(Ljava/lang/String;Ljava/lang/String;)V", at = @At("RETURN"))
    private void init(String p_135811_, String p_135812_, CallbackInfo ci) {
        this.nobtg$val = p_135811_ + ":" + p_135812_;
    }
}
