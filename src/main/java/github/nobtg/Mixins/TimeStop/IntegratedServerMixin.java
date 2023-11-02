package github.nobtg.Mixins.TimeStop;

import com.mojang.datafixers.DataFixer;
import github.nobtg.Utils.TimeStop.TimeStop;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.function.BooleanSupplier;

@Mixin(value = IntegratedServer.class, priority = 0x7fffffff)
public abstract class IntegratedServerMixin extends MinecraftServer {
    @Shadow
    private boolean paused;

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final private Minecraft minecraft;

    @Shadow private int previousSimulationDistance;

    @Shadow protected abstract void tickPaused();

    public IntegratedServerMixin(Thread p_236723_, LevelStorageSource.LevelStorageAccess p_236724_, PackRepository p_236725_, WorldStem p_236726_, Proxy p_236727_, DataFixer p_236728_, Services p_236729_, ChunkProgressListenerFactory p_236730_) {
        super(p_236723_, p_236724_, p_236725_, p_236726_, p_236727_, p_236728_, p_236729_, p_236730_);
    }

    @Inject(method = "tickServer", at = @At("HEAD"), cancellable = true)
    public void tick(BooleanSupplier p_120049_, CallbackInfo ci) {
        boolean flag = this.paused;
        this.paused = Minecraft.getInstance().isPaused();
        // Start Modify
        if (TimeStop.get()) this.paused = false;
        // End Modify
        ProfilerFiller profilerfiller = this.getProfiler();
        if (!flag && this.paused) {
            profilerfiller.push("autoSave");
            LOGGER.info("Saving and pausing game...");
            this.saveEverything(false, false, false);
            profilerfiller.pop();
        }
        boolean flag1 = Minecraft.getInstance().getConnection() != null;
        if (flag1 && this.paused) {
            this.tickPaused();
        } else {
            if (flag && !this.paused)
                this.forceTimeSynchronization();
            super.tickServer(p_120049_);
            int i = Math.max(2, this.minecraft.options.renderDistance().get());
            if (i != this.getPlayerList().getViewDistance()) {
                LOGGER.info("Changing view distance to {}, from {}", i, this.getPlayerList().getViewDistance());
                this.getPlayerList().setViewDistance(i);
            }
            int j = Math.max(2, this.minecraft.options.simulationDistance().get());
            if (j != this.previousSimulationDistance) {
                LOGGER.info("Changing simulation distance to {}, from {}", j, this.previousSimulationDistance);
                this.getPlayerList().setSimulationDistance(j);
                this.previousSimulationDistance = j;
            }
        }
        ci.cancel();
    }
}
