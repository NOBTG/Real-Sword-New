package github.nobtg.Mixins.Servers;

import com.mojang.datafixers.DataFixer;
import github.nobtg.Utils.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;

@Mixin(value = GameTestServer.class, priority = 0x7fffffff)
public abstract class GameTestServerMixin extends MinecraftServer {
    @Shadow @Final private BlockPos spawnPos;

    @Shadow @Final private static Logger LOGGER;

    public GameTestServerMixin(Thread p_236723_, LevelStorageSource.LevelStorageAccess p_236724_, PackRepository p_236725_, WorldStem p_236726_, Proxy p_236727_, DataFixer p_236728_, Services p_236729_, ChunkProgressListenerFactory p_236730_) {
        super(p_236723_, p_236724_, p_236725_, p_236726_, p_236727_, p_236728_, p_236729_, p_236730_);
    }

    @Inject(method = "initServer", at = @At(value = "HEAD"), cancellable = true)
    private void initServer(CallbackInfoReturnable<Boolean> cir) {
        GameTestServer this_ = (GameTestServer) (Object) this;
        // Start Modify
        this_.setPlayerList(EntityUtil.newList(this_.getPlayerList()));
        // End Modify
        if (!ServerLifecycleHooks.handleServerAboutToStart(this_)) cir.setReturnValue(false);
        this.loadLevel();
        ServerLevel serverlevel = this.overworld();
        serverlevel.setDefaultSpawnPos(this.spawnPos, 0.0F);
        serverlevel.setWeatherParameters(20000000, 20000000, false, false);
        LOGGER.info("Started game test server");
        cir.setReturnValue(ServerLifecycleHooks.handleServerStarting(this_));
    }
}
