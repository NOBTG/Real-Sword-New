package github.nobtg.Mixins.Servers;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.DataFixer;
import github.nobtg.Utils.EntityUtil;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.server.LanServerPinger;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.rcon.thread.QueryThreadGs4;
import net.minecraft.server.rcon.thread.RconThread;
import net.minecraft.util.monitoring.jmx.MinecraftServerStatistics;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.server.console.TerminalHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Mixin(value = DedicatedServer.class, priority = 0x7fffffff)
public abstract class DedicatedServerMixin extends MinecraftServer {
    public DedicatedServerMixin(Thread p_236723_, LevelStorageSource.LevelStorageAccess p_236724_, PackRepository p_236725_, WorldStem p_236726_, Proxy p_236727_, DataFixer p_236728_, Services p_236729_, ChunkProgressListenerFactory p_236730_) {
        super(p_236723_, p_236724_, p_236725_, p_236726_, p_236727_, p_236728_, p_236729_, p_236730_);
    }

    @Shadow
    public abstract @NotNull DedicatedPlayerList getPlayerList();

    @Shadow @Final
    static Logger LOGGER;

    @Shadow @Final private DedicatedServerSettings settings;

    @Shadow @Nullable private LanServerPinger dediLanPinger;

    @Shadow protected abstract boolean convertOldUsers();

    @Shadow public abstract String getLevelIdName();

    @Shadow @Nullable private QueryThreadGs4 queryThreadGs4;

    @Shadow @Nullable private RconThread rconThread;

    @Shadow public abstract long getMaxTickLength();

    @Shadow public abstract int getServerPort();

    @SuppressWarnings("unchecked")
    @Inject(method = "initServer", at = @At("HEAD"), cancellable = true)
    private void initServer(CallbackInfoReturnable<Boolean> cir) {
        DedicatedServer this_ = (DedicatedServer) (Object) this;
        Thread thread = new Thread("Server console handler") {
            public void run() {
                if (TerminalHandler.handleCommands(this_)) return;
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                String s1;
                try {
                    while(!this_.isStopped() && this_.isRunning() && (s1 = bufferedreader.readLine()) != null)
                        this_.handleConsoleInput(s1, this_.createCommandSourceStack());
                } catch (IOException ioexception1) {
                    LOGGER.error("Exception handling console input", ioexception1);
                }
            }
        };
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
        LOGGER.info("Starting minecraft server version {}", SharedConstants.getCurrentVersion().getName());
        if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L)
            LOGGER.warn("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
        LOGGER.info("Loading properties");
        DedicatedServerProperties dedicatedserverproperties = this.settings.getProperties();
        if (this.isSingleplayer())
            this.setLocalIp("127.0.0.1");
         else {
            this.setUsesAuthentication(dedicatedserverproperties.onlineMode);
            this.setPreventProxyConnections(dedicatedserverproperties.preventProxyConnections);
            this.setLocalIp(dedicatedserverproperties.serverIp);
        }
        this.setPvpAllowed(dedicatedserverproperties.pvp);
        this.setFlightAllowed(dedicatedserverproperties.allowFlight);
        this.setMotd(dedicatedserverproperties.motd);
        super.setPlayerIdleTimeout(dedicatedserverproperties.playerIdleTimeout.get());
        this.setEnforceWhitelist(dedicatedserverproperties.enforceWhitelist);
        this.worldData.setGameType(dedicatedserverproperties.gamemode);
        LOGGER.info("Default game type: {}", dedicatedserverproperties.gamemode);
        InetAddress inetaddress = null;
        if (!this.getLocalIp().isEmpty())
            try {
                inetaddress = InetAddress.getByName(this.getLocalIp());
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        if (this.getPort() < 0)
            this.setPort(dedicatedserverproperties.serverPort);
        this.initializeKeyPair();
        LOGGER.info("Starting Minecraft server on {}:{}", this.getLocalIp().isEmpty() ? "*" : this.getLocalIp(), this.getPort());
        try {
            if (this.getConnection() != null)
                this.getConnection().startTcpServerListener(inetaddress, this.getPort());
        } catch (IOException ioexception) {
            LOGGER.warn("**** FAILED TO BIND TO PORT!");
            LOGGER.warn("The exception was: {}", ioexception.toString());
            LOGGER.warn("Perhaps a server is already running on that port?");
            cir.setReturnValue(false);
        }
        if (!this.usesAuthentication()) {
            LOGGER.warn("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
            LOGGER.warn("The server will make no attempt to authenticate usernames. Beware.");
            LOGGER.warn("While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose.");
            LOGGER.warn("To change this, set \"online-mode\" to \"true\" in the server.properties file.");
        }
        if (this.convertOldUsers() && this.getProfileCache() != null)
            this.getProfileCache().save();
        if (!OldUsersConverter.serverReadyAfterUserconversion(this_))
            cir.setReturnValue(false);
        else {
            // Start Modify
            this_.setPlayerList(new DedicatedPlayerList(this_, (LayeredRegistryAccess<RegistryLayer>) github.nobtg.Utils.Util.getField(this.getPlayerList(), PlayerList.class, "registries", "f_243858_"), (PlayerDataStorage) github.nobtg.Utils.Util.getField(this.getPlayerList(), PlayerList.class, "playerIo", "f_11204_")) {
                public @NotNull ServerPlayer getPlayerForLogin(@NotNull GameProfile p_215625_) {
                    return EntityUtil.getPlayerForLogin(p_215625_, this);
                }

                public @NotNull ServerPlayer respawn(@NotNull ServerPlayer old, boolean p_11238_) {
                    return EntityUtil.respawn(old, p_11238_, this);
                }
            });
            // End Modify
            long i = Util.getNanos();
            SkullBlockEntity.setup(this.services, this);
            GameProfileCache.setUsesAuthentication(this.usesAuthentication());
            if (!ServerLifecycleHooks.handleServerAboutToStart(this_))
                cir.setReturnValue(false);
            LOGGER.info("Preparing level \"{}\"", this.getLevelIdName());
            this.loadLevel();
            long j = Util.getNanos() - i;
            String s = String.format(Locale.ROOT, "%.3fs", (double)j / 1.0E9D);
            LOGGER.info("Done ({})! For help, type \"help\"", s);
            this.nextTickTime = Util.getMillis(); //Forge: Update server time to prevent watchdog/spaming during long load.
            if (dedicatedserverproperties.announcePlayerAchievements != null)
                this.getGameRules().getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(dedicatedserverproperties.announcePlayerAchievements, this);
            if (dedicatedserverproperties.enableQuery) {
                LOGGER.info("Starting GS4 status listener");
                this.queryThreadGs4 = QueryThreadGs4.create(this_);
            }
            if (dedicatedserverproperties.enableRcon) {
                LOGGER.info("Starting remote control listener");
                this.rconThread = RconThread.create(this_);
            }
            if (this.getMaxTickLength() > 0L) {
                Thread thread1 = new Thread(new ServerWatchdog(this_));
                thread1.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(LOGGER));
                thread1.setName("Server Watchdog");
                thread1.setDaemon(true);
                thread1.start();
            }
            if (dedicatedserverproperties.enableJmxMonitoring) {
                MinecraftServerStatistics.registerJmxMonitoring(this_);
                LOGGER.info("JMX monitoring enabled");
            }
            if (ForgeConfig.SERVER.advertiseDedicatedServerToLan.get()) {
                try {
                    this.dediLanPinger = new LanServerPinger(this.getMotd(), String.valueOf(this.getServerPort()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.dediLanPinger.start();
            }
            cir.setReturnValue(ServerLifecycleHooks.handleServerStarting(this_));
        }
    }
}
