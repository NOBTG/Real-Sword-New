package github.nobtg.Mixins.Servers;

import com.mojang.authlib.GameProfile;
import github.nobtg.Utils.EntityUtil;
import github.nobtg.Utils.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedPlayerList;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = IntegratedServer.class, priority = 0x7fffffff)
public abstract class IntegratedServerMixin {
    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void initServer(Thread p_235248_, Minecraft p_235249_, LevelStorageSource.LevelStorageAccess p_235250_, PackRepository p_235251_, WorldStem p_235252_, Services p_235253_, ChunkProgressListenerFactory p_235254_, CallbackInfo ci) {
        IntegratedServer this_ = (IntegratedServer) (Object) this;
        if (this_.getPlayerList() instanceof IntegratedPlayerList) {
            this_.setPlayerList(new IntegratedPlayerList(this_, (LayeredRegistryAccess<RegistryLayer>) Util.getField(this_.getPlayerList(), PlayerList.class, "registries", "f_243858_"), (PlayerDataStorage) Util.getField(this_.getPlayerList(), PlayerList.class, "playerIo", "f_11204_")) {
                public @NotNull ServerPlayer getPlayerForLogin(@NotNull GameProfile p_215625_) {
                    return EntityUtil.getPlayerForLogin(p_215625_, this);
                }

                public @NotNull ServerPlayer respawn(@NotNull ServerPlayer old, boolean p_11238_) {
                    return EntityUtil.respawn(old, p_11238_, this);
                }
            });
        } else this_.setPlayerList(EntityUtil.newList(this_.getPlayerList()));
    }
}
