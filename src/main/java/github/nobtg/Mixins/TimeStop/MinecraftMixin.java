package github.nobtg.Mixins.TimeStop;

import com.mojang.logging.LogUtils;
import github.nobtg.Utils.Util;
import github.nobtg.Utils.TimeStop.TimeStop;
import github.nobtg.Utils.TimeStop.TimeStopPacket;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@Mixin(value = Minecraft.class, priority = 0x7fffffff)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public ClientLevel level;
    @Shadow
    @Final
    public Gui gui;
    @Shadow
    @Final
    public GameRenderer gameRenderer;
    @Shadow
    @Nullable
    public Screen screen;
    @Shadow
    @Nullable
    public LocalPlayer player;
    @Shadow
    private int rightClickDelay;
    @Shadow
    private ProfilerFiller profiler;
    @Shadow
    private volatile boolean pause;
    @Shadow
    protected int missTime;
    @Shadow
    @Nullable
    private Overlay overlay;
    @Final
    @Mutable
    @Shadow
    private Timer timer;
    @Final
    @Shadow
    private SoundManager soundManager;

    @Shadow
    public abstract void setScreen(@Nullable Screen var1);

    @Shadow
    protected abstract void handleKeybinds();

    @Shadow
    public abstract void tick();

    @Shadow
    private Thread gameThread;

    @Shadow
    private volatile boolean running;

    @Shadow
    @Nullable
    private Supplier<CrashReport> delayedCrash;

    @Shadow
    public static void crash(CrashReport p_91333_) {
        throw new RuntimeException();
    }

    @Shadow
    protected abstract boolean shouldRenderFpsPie();

    @Shadow
    protected abstract ProfilerFiller constructProfiler(boolean p_167971_, @org.jetbrains.annotations.Nullable SingleTickProfiler p_167972_);

    @Shadow
    private MetricsRecorder metricsRecorder;

    @Shadow
    protected abstract void runTick(boolean p_91384_);

    @Shadow
    protected abstract void finishProfilers(boolean p_91339_, @org.jetbrains.annotations.Nullable SingleTickProfiler p_91340_);

    @Shadow
    public abstract void emergencySave();

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    public abstract CrashReport fillReport(CrashReport p_91355_);

    @Shadow @Final public KeyboardHandler keyboardHandler;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo ci) {
        if (TimeStop.get()) {
            this.pause = true;
            if (this.rightClickDelay > 0) --this.rightClickDelay;
            this.profiler.push("gui");
            this.profiler.pop();
            if (this.screen == null && this.player != null) {
                if (this.player.isDeadOrDying() && !(this.screen instanceof DeathScreen)) this.setScreen(null);
                else if (this.player.isSleeping() && this.level != null) this.setScreen(new InBedChatScreen());
            } else if (this.screen instanceof InBedChatScreen inbedchatscreen && this.player != null && !this.player.isSleeping())
                inbedchatscreen.onPlayerWokeUp();
            if (this.screen != null) this.missTime = 10000;
            if (this.screen != null)
                Screen.wrapScreenError(() -> this.screen.tick(), "Ticking screen", this.screen.getClass().getCanonicalName());
            if (this.overlay == null && this.screen == null) {
                this.handleKeybinds();
                if (this.missTime > 0) --this.missTime;
            }
            if (this.level != null) {
                ((EntityTickList) Util.getField(this.level, ClientLevel.class, "tickingEntities", "f_171630_")).forEach((s) -> {
                    if (!s.isRemoved() && !s.isPassenger()) {
                        if (s instanceof Player && s != this.player)
                            this.level.guardEntityTick(this.level::tickNonPassenger, s);
                        if (s.tickCount < 1) this.level.guardEntityTick(this.level::tickNonPassenger, s);
                    }
                });
            }
            Util.fieldSetField(this.timer, Timer.class, "msPerTick", TimeStop.perTick, "f_92521_");
            this.soundManager.tick(false);
            ci.cancel();
        }
    }

    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    public void run(CallbackInfo ci) {
        this.gameThread = Thread.currentThread();
        if (Runtime.getRuntime().availableProcessors() > 4) this.gameThread.setPriority(10);
        try {
            boolean flag = false;
            while (this.running) {
                if (this.delayedCrash != null) {
                    crash(this.delayedCrash.get());
                    return;
                }
                try {
                    SingleTickProfiler singletickprofiler = SingleTickProfiler.createTickProfiler("Renderer");
                    boolean flag1 = this.shouldRenderFpsPie();
                    this.profiler = this.constructProfiler(flag1, singletickprofiler);
                    this.profiler.startTick();
                    this.metricsRecorder.startTick();
                    this.runTick(!flag);
                    // Start Modify
                    if (TimeStop.get()) {
                        for (int i = 0; i < TimeStop.timer.advanceTime(TimeStop.realMillis); ++i) {
                            if (this.level != null && this.player != null) {
                                this.level.guardEntityTick(this.level::tickNonPassenger, this.player);
                                this.gui.tick(Minecraft.getInstance().isPaused());
                                this.gameRenderer.itemInHandRenderer.tick();
                                this.gameRenderer.tick();
                                if (this.screen != null) this.screen.tick();
                                this.tick();
                                this.keyboardHandler.tick();
                            }
                        }
                    }
                    if (Minecraft.getInstance().level == null && TimeStop.realMillis > 3000L)
                        TimeStop.setIsTimeStop(false);
                    // End Modify
                    this.metricsRecorder.endTick();
                    this.profiler.endTick();
                    this.finishProfilers(flag1, singletickprofiler);
                } catch (OutOfMemoryError outofmemoryerror) {
                    if (flag) throw outofmemoryerror;
                    this.emergencySave();
                    this.setScreen(new OutOfMemoryScreen());
                    System.gc();
                    LOGGER.error(LogUtils.FATAL_MARKER, "Out of memory", outofmemoryerror);
                    flag = true;
                }
            }
        } catch (ReportedException reportedexception) {
            this.fillReport(reportedexception.getReport());
            this.emergencySave();
            LOGGER.error(LogUtils.FATAL_MARKER, "Reported exception thrown!", reportedexception);
            crash(reportedexception.getReport());
        } catch (Throwable throwable) {
            CrashReport crashreport = this.fillReport(new CrashReport("Unexpected error", throwable));
            LOGGER.error(LogUtils.FATAL_MARKER, "Unreported exception thrown!", throwable);
            this.emergencySave();
            crash(crashreport);
        }
        ci.cancel();
    }

    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
    private void clearLevel(Screen p_91321_, CallbackInfo ci) {
        if (this.level != null) if (this.level.isClientSide) {
            TimeStop.setIsTimeStop(false);
        } else if (player != null)
            Util.getPacketChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new TimeStopPacket(TimeStop.get(), player.getId()));
    }
}
