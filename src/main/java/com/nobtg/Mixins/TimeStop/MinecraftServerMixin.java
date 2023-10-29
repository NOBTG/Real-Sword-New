package com.nobtg.Mixins.TimeStop;

import com.nobtg.Utils.TimeStop.TimeStop;
import net.minecraft.CrashReport;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.function.BooleanSupplier;

@Mixin(value = MinecraftServer.class, priority = 0x7fffffff)
public abstract class MinecraftServerMixin {
    @Shadow
    public abstract void tickChildren(BooleanSupplier paramBooleanSupplier);

    @Shadow public abstract void tickServer(BooleanSupplier p_129871_);

    @Shadow protected abstract boolean haveTime();

    @Shadow private ProfilerFiller profiler;

    @Shadow private boolean mayHaveDelayedTasks;

    @Shadow private long delayedTasksMaxNextTickTime;

    @Shadow protected abstract void waitUntilNextTick();

    @Shadow protected abstract void endMetricsRecordingTick();

    @Shadow protected long nextTickTime;

    @Shadow protected abstract void startMetricsRecordingTick();

    @Shadow private volatile boolean isReady;

    @Shadow private float averageTickTime;

    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract SystemReport fillSystemReport(SystemReport p_177936_);

    @Shadow
    private static CrashReport constructOrExtractCrashReport(Throwable p_206569_) {
        throw new RuntimeException();
    }

    @Shadow public abstract File getServerDirectory();

    @Shadow public abstract void onServerCrash(CrashReport p_129874_);

    @Shadow private boolean stopped;

    @Shadow public abstract void stopServer();

    @Shadow public abstract void onServerExit();

    @Shadow @Final protected Services services;

    @Shadow protected abstract boolean initServer() throws IOException;

    @Shadow @Nullable private ServerStatus.Favicon statusIcon;

    @Shadow @Nullable private ServerStatus status;

    @Shadow protected abstract ServerStatus buildServerStatus();

    @Shadow protected abstract Optional<ServerStatus.Favicon> loadStatusIcon();

    @Shadow protected abstract void resetStatusCache(ServerStatus status);

    @Shadow private volatile boolean running;

    @Shadow private long lastOverloadWarning;

    @Shadow private boolean debugCommandProfilerDelayStart;

    @Shadow @Nullable private MinecraftServer.TimeProfiler debugCommandProfiler;

    @Shadow private int tickCount;

    @Inject(method = "runServer", at = @At("HEAD"), cancellable = true)
    public void run(CallbackInfo ci) {
        MinecraftServer this_ = (MinecraftServer) (Object) this;
        try {
            if (!this.initServer())
                throw new IllegalStateException("Failed to initialize server");
            ServerLifecycleHooks.handleServerStarted(this_);
            this.nextTickTime = Util.getMillis();
            this.statusIcon = this.loadStatusIcon().orElse(null);
            this.status = this.buildServerStatus();
            resetStatusCache(status);
            while (this.running) {
                long i = Util.getMillis() - this.nextTickTime;
                if (i > 2000L && this.nextTickTime - this.lastOverloadWarning >= 15000L) {
                    long j = i / 50L;
                    LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", i, j);
                    this.nextTickTime += j * 50L;
                    this.lastOverloadWarning = this.nextTickTime;
                }
                if (this.debugCommandProfilerDelayStart) {
                    this.debugCommandProfilerDelayStart = false;
                    Constructor<MinecraftServer.TimeProfiler> method = MinecraftServer.TimeProfiler.class.getDeclaredConstructor(long.class, int.class);
                    this.debugCommandProfiler = method.newInstance(Util.getNanos(), this.tickCount);
                }
                this.nextTickTime += 50L;
                this.startMetricsRecordingTick();
                this.profiler.push("tick");
                // Start Modify
                if (TimeStop.get())
                    tickChildren(() -> true);
                // End Modify
                this.tickServer(this::haveTime);
                this.profiler.popPush("nextTickWait");
                this.mayHaveDelayedTasks = true;
                this.delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + 50L, this.nextTickTime);
                this.waitUntilNextTick();
                this.profiler.pop();
                this.endMetricsRecordingTick();
                this.isReady = true;
                JvmProfiler.INSTANCE.onServerTick(this.averageTickTime);
            }
            ServerLifecycleHooks.handleServerStopping(this_);
            ServerLifecycleHooks.expectServerStopped();
        } catch (Throwable throwable1) {
            LOGGER.error("Encountered an unexpected exception", throwable1);
            CrashReport crashreport = constructOrExtractCrashReport(throwable1);
            this.fillSystemReport(crashreport.getSystemReport());
            File file1 = new File(new File(this.getServerDirectory(), "crash-reports"), "crash-" + Util.getFilenameFormattedDateTime() + "-server.txt");
            if (crashreport.saveToFile(file1))
                LOGGER.error("This crash report has been saved to: {}", file1.getAbsolutePath());
             else LOGGER.error("We were unable to save this crash report to disk.");
            ServerLifecycleHooks.expectServerStopped();
            this.onServerCrash(crashreport);
        } finally {
            try {
                this.stopped = true;
                this.stopServer();
            } catch (Throwable throwable) {
                LOGGER.error("Exception stopping the server", throwable);
            } finally {
                this.services.profileCache().clearExecutor();
                ServerLifecycleHooks.handleServerStopped(this_);
                this.onServerExit();
            }
        }
        ci.cancel();
    }
}