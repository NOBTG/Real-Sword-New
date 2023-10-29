package com.nobtg.Mixins.Screens;

import com.mojang.blaze3d.platform.Window;
import com.nobtg.RealSwordMod;
import com.nobtg.Utils.GetterAndSetters;
import com.nobtg.Utils.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = Minecraft.class, priority = 0x7fffffff)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public LocalPlayer player;
    @Shadow
    private static int fps;
    @Shadow
    @Nullable
    public Screen screen;

    @Shadow
    public abstract void updateTitle();

    @Shadow
    protected abstract String createTitle();

    @Shadow
    @Final
    private Window window;

    @Shadow
    @Nullable
    public HitResult hitResult;
    @Shadow
    @Nullable
    public MultiPlayerGameMode gameMode;
    @Shadow
    @Nullable
    public ClientLevel level;
    @Shadow
    protected int missTime;
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    public Options options;
    @Unique
    public boolean nobtg$NoDrawScreen;

    @SuppressWarnings("unchecked")
    @Inject(method = "updateTitle", at = @At("HEAD"), cancellable = true)
    private void updateTitle(@NotNull CallbackInfo ci) {
        Float EntityDataHealth = this.player != null ? this.player.getEntityData().get((EntityDataAccessor<Float>) Util.getField(this.player, LivingEntity.class, "DATA_HEALTH_ID", "f_20961_")) : null;
        String screen = this.screen != null ? this.screen.getClass().getSimpleName() : null;
        Float Health = this.player != null ? this.player.getHealth() : null;
        String MoreInfo = screen == null ? "" : " Screen: " + screen + "(" + nobtg$CheckNull(this.screen.getClass().getName()).replace("." + nobtg$CheckNull(this.screen.getClass().getSimpleName()), "") + ")";
        this.window.setTitle(this.createTitle() + " FPS: " + fps + (Health == null ? "" : (" Health: " + Health)) + (EntityDataHealth == null ? "" : (" EntityDataHealth: " + EntityDataHealth)) + MoreInfo);
        ci.cancel();
    }

    @Unique
    private @NotNull String nobtg$CheckNull(String obj) {
        return (obj != null) ? obj : "null";
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        this.updateTitle();
        if (this.player != null && GetterAndSetters.getRealEntities().contains(this.player) && Util.getAutoCombat(this.player))
            nobtg$NoDrawScreen = false;
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void startAttack(CallbackInfoReturnable<Boolean> cir) {
        if (this.missTime > 0)
            cir.setReturnValue(false);
        else if (this.hitResult == null) {
            LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
            if (this.gameMode != null && this.gameMode.hasMissTime())
                this.missTime = 10;
            cir.setReturnValue(false);
        } else if (this.player != null && this.player.isHandsBusy())
            cir.setReturnValue(false);
         else {
            ItemStack itemstack = this.player == null ? ItemStack.EMPTY : this.player.getItemInHand(InteractionHand.MAIN_HAND);
            if (this.level != null && !itemstack.isItemEnabled(this.level.enabledFeatures()))
                cir.setReturnValue(false);
            else {
                boolean flag = false;
                var inputEvent = ForgeHooksClient.onClickInput(0, this.options.keyAttack, InteractionHand.MAIN_HAND);
                if (!inputEvent.isCanceled()) switch (this.hitResult.getType()) {
                    case ENTITY:
                        if (this.gameMode != null)
                            this.gameMode.attack(this.player, ((EntityHitResult) this.hitResult).getEntity());
                        break;
                    case BLOCK:
                        BlockHitResult blockhitresult = (BlockHitResult) this.hitResult;
                        BlockPos blockpos = blockhitresult.getBlockPos();
                        if (!this.level.isEmptyBlock(blockpos) && this.gameMode != null) {
                            this.gameMode.startDestroyBlock(blockpos, blockhitresult.getDirection());
                            if (this.level.getBlockState(blockpos).isAir())
                                flag = true;
                            break;
                        }
                    case MISS:
                        if (this.gameMode != null && this.gameMode.hasMissTime())
                            this.missTime = 10;
                        this.player.resetAttackStrengthTicker();
                        ForgeHooks.onEmptyLeftClick(this.player);
                        // Start Modify
                        if (this.player != null && GetterAndSetters.getRealEntities().contains(this.player) && !Util.getAutoCombat(this.player) && this.player.getMainHandItem().getItem() == RealSwordMod.REAL_SWORD.get() && this.player.isShiftKeyDown())
                            nobtg$NoDrawScreen = !nobtg$NoDrawScreen;
                        // End Modify
                }
                if (inputEvent.shouldSwingHand()) this.player.swing(InteractionHand.MAIN_HAND);
                cir.setReturnValue(flag);
            }
        }
    }
}
