package github.nobtg.Mixins.FixFly;

import github.nobtg.Utils.EntityUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(value = ServerPlayerGameMode.class, priority = 0x7fffffff)
public abstract class ServerPlayerGameModeMixin {
    @Shadow
    @Final
    protected ServerPlayer player;

    @Shadow @Nullable private GameType previousGameModeForPlayer;

    @Shadow private GameType gameModeForPlayer;

    @Inject(method = "setGameModeForPlayer", at = @At(value = "HEAD"), cancellable = true)
    private void updatePlayerAbilities(@NotNull GameType p_9274_, GameType p_9275_, CallbackInfo ci) {
        this.previousGameModeForPlayer = p_9275_;
        this.gameModeForPlayer = p_9274_;
        p_9274_.updatePlayerAbilities(this.player.getAbilities());
        // Start Modify
        if (EntityUtil.getRealEntities().contains(this.player)) this.player.getAbilities().flying = !this.player.getAbilities().flying && !this.player.onGround();
        // End Modify
        ci.cancel();
    }
}
