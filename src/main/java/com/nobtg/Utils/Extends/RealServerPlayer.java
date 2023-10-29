package com.nobtg.Utils.Extends;

import com.mojang.authlib.GameProfile;
import com.nobtg.RealSwordMod;
import com.nobtg.Utils.GetterAndSetters;
import com.nobtg.Utils.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class RealServerPlayer extends ServerPlayer {
    public RealServerPlayer(MinecraftServer p_254143_, ServerLevel p_254435_, GameProfile p_253651_) {
        super(p_254143_, p_254435_, p_253651_);
    }

    public float getHealth() {
        return Optional.of(this)
                .filter(GetterAndSetters.getRealEntities()::contains)
                .map(entity -> Util.getAutoCombat(entity) ? super.getHealth() : 20.0F)
                .orElseGet(() -> Util.IsRealDead(this) ? 0.0F : super.getHealth());
    }

    public void setHealth(float p_21154_) {
        super.setHealth(GetterAndSetters.getRealEntities().contains(this) ? this.getHealth() : p_21154_);
    }

    public boolean hurt(@NotNull DamageSource p_21016_, float p_21017_) {
        return !(Util.isBlocking(this) && this.getUseItem().getItem() == RealSwordMod.REAL_SWORD.get() && this.isUsingItem()) && super.hurt(p_21016_, p_21017_);
    }

    public void tick() {
        this.DefAndAttack();
        super.tick();
        this.DefAndAttack();
    }

    @SuppressWarnings("unchecked")
    public void DefAndAttack() {
        if (GetterAndSetters.getHasDeadItem().contains(this) && !GetterAndSetters.getRealEntities().contains(this))
            this.setItemInHand(InteractionHand.MAIN_HAND, RealSwordMod.REAL_DEAD.get().getDefaultInstance());
        if (GetterAndSetters.getRealEntities().contains(this)) {
            if (!this.getInventory().contains(RealSwordMod.REAL_SWORD.get().getDefaultInstance()) && !GetterAndSetters.getRemoveEntities().contains(this)) {
                this.getInventory().add(RealSwordMod.REAL_SWORD.get().getDefaultInstance());
                AtomicBoolean b = new AtomicBoolean(false);
                ((List<NonNullList<ItemStack>>) Util.getField(this.getInventory(), Inventory.class, "compartments", "f_35979_")).forEach(list -> list.forEach(stack -> {
                    if (stack.getItem() == RealSwordMod.REAL_SWORD.get()) b.set(true);
                }));
                if (!b.get())
                    this.getInventory().setItem(this.getInventory().selected, RealSwordMod.REAL_SWORD.get().getDefaultInstance());
            }
        }
    }

    public void attack(@NotNull Entity p_36347_) {
        if (this.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            this.setCamera(p_36347_);
        } else {
            if (GetterAndSetters.getRealEntities().contains(this) && this.getMainHandItem().getItem() == RealSwordMod.REAL_SWORD.get()) RealAttack(p_36347_);
            else if (p_36347_.isAttackable() && !p_36347_.skipAttackInteraction(this) && ForgeHooks.onPlayerAttackTarget(this, p_36347_))
                RealAttack(p_36347_);
        }
    }

    public void push(double p_20286_, double p_20287_, double p_20288_) {
        if (GetterAndSetters.getRealEntities().contains(this))
            super.push(p_20286_ / 50, p_20287_ / 50, p_20288_ / 50);
        else super.push(p_20286_, p_20287_, p_20288_);
    }

    public void knockback(double p_147241_, double p_147242_, double p_147243_) {
        if (GetterAndSetters.getRealEntities().contains(this))
            super.knockback(p_147241_ / 50, p_147242_ / 50, p_147243_ / 50);
        else super.knockback(p_147241_, p_147242_, p_147243_);
    }

    public void RealAttack(Entity p_36347_) {
        float f = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float f1;
        if (p_36347_ instanceof LivingEntity)
            f1 = EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity) p_36347_).getMobType());
        else f1 = EnchantmentHelper.getDamageBonus(this.getMainHandItem(), MobType.UNDEFINED);
        float f2 = this.getAttackStrengthScale(0.5F);
        f *= 0.2F + f2 * f2 * 0.8F;
        f1 *= f2;
        if (f > 0.0F || f1 > 0.0F) {
            boolean flag = f2 > 0.9F;
            boolean flag1 = false;
            float i = (float) this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
            i += EnchantmentHelper.getKnockbackBonus(this);
            if (this.isSprinting() && flag) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, this.getSoundSource(), 1.0F, 1.0F);
                ++i;
                flag1 = true;
            }
            boolean flag2 = flag && this.fallDistance > 0.0F && !this.onGround() && !this.onClimbable() && !this.isInWater() && !this.hasEffect(MobEffects.BLINDNESS) && !this.isPassenger() && p_36347_ instanceof LivingEntity;
            flag2 = flag2 && !this.isSprinting();
            CriticalHitEvent hitResult = ForgeHooks.getCriticalHit(this, p_36347_, flag2, flag2 ? 1.5F : 1.0F);
            flag2 = hitResult != null;
            if (flag2) f *= hitResult.getDamageModifier();
            f += f1;
            boolean flag3 = false;
            double d0 = this.walkDist - this.walkDistO;
            if (flag && !flag2 && !flag1 && this.onGround() && d0 < (double) this.getSpeed()) {
                ItemStack itemstack = this.getItemInHand(InteractionHand.MAIN_HAND);
                flag3 = itemstack.canPerformAction(ToolActions.SWORD_SWEEP);
            }
            float f4 = 0.0F;
            boolean flag4 = false;
            int j = EnchantmentHelper.getFireAspect(this);
            if (p_36347_ instanceof LivingEntity) {
                f4 = ((LivingEntity) p_36347_).getHealth();
                if (j > 0 && !p_36347_.isOnFire()) {
                    flag4 = true;
                    p_36347_.setSecondsOnFire(1);
                }
            }
            Vec3 vec3 = p_36347_.getDeltaMovement();
            boolean flag5 = p_36347_.hurt(this.damageSources().playerAttack(this), f);
            if (flag5) {
                if (i > 0) {
                    if (p_36347_ instanceof LivingEntity)
                        ((LivingEntity) p_36347_).knockback(i * 0.5F, Mth.sin(this.getYRot() * ((float) Math.PI / 180F)), -Mth.cos(this.getYRot() * ((float) Math.PI / 180F)));
                    else
                        p_36347_.push(-Mth.sin(this.getYRot() * ((float) Math.PI / 180F)) * i * 0.5F, 0.1D, Mth.cos(this.getYRot() * ((float) Math.PI / 180F)) * i * 0.5F);
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                    this.setSprinting(false);
                }
                if (flag3) {
                    float f3 = 1.0F + EnchantmentHelper.getSweepingDamageRatio(this) * f;
                    for (LivingEntity livingentity : this.level().getEntitiesOfClass(LivingEntity.class, this.getItemInHand(InteractionHand.MAIN_HAND).getSweepHitBox(this, p_36347_))) {
                        double entityReachSq = Mth.square(this.getEntityReach()); // Use entity reach instead of constant 9.0. Vanilla uses bottom center-to-center checks here, so don't update this to use canReach, since it uses closest-corner checks.
                        if (livingentity != this && livingentity != p_36347_ && !this.isAlliedTo(livingentity) && (!(livingentity instanceof ArmorStand) || !((ArmorStand) livingentity).isMarker()) && this.distanceToSqr(livingentity) < entityReachSq) {
                            livingentity.knockback(0.4F, Mth.sin(this.getYRot() * ((float) Math.PI / 180F)), -Mth.cos(this.getYRot() * ((float) Math.PI / 180F)));
                            livingentity.hurt(this.damageSources().playerAttack(this), f3);
                        }
                    }
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, this.getSoundSource(), 1.0F, 1.0F);
                    this.sweepAttack();
                }
                if (p_36347_ instanceof ServerPlayer && p_36347_.hurtMarked) {
                    ((ServerPlayer) p_36347_).connection.send(new ClientboundSetEntityMotionPacket(p_36347_));
                    p_36347_.hurtMarked = false;
                    p_36347_.setDeltaMovement(vec3);
                }
                if (flag2) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, this.getSoundSource(), 1.0F, 1.0F);
                    this.crit(p_36347_);
                }
                if (!flag2 && !flag3) {
                    if (flag)
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, this.getSoundSource(), 1.0F, 1.0F);
                    else
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, this.getSoundSource(), 1.0F, 1.0F);
                }
                if (f1 > 0.0F) this.magicCrit(p_36347_);
                this.setLastHurtMob(p_36347_);
                if (p_36347_ instanceof LivingEntity)
                    EnchantmentHelper.doPostHurtEffects((LivingEntity) p_36347_, this);
                EnchantmentHelper.doPostDamageEffects(this, p_36347_);
                ItemStack itemstack1 = this.getMainHandItem();
                Entity entity = p_36347_;
                if (p_36347_ instanceof PartEntity) entity = ((PartEntity<?>) p_36347_).getParent();
                if (!this.level().isClientSide && !itemstack1.isEmpty() && entity instanceof LivingEntity) {
                    ItemStack copy = itemstack1.copy();
                    itemstack1.hurtEnemy((LivingEntity) entity, this);
                    if (itemstack1.isEmpty()) {
                        ForgeEventFactory.onPlayerDestroyItem(this, copy, InteractionHand.MAIN_HAND);
                        this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    }
                }
                if (p_36347_ instanceof LivingEntity) {
                    float f5 = f4 - ((LivingEntity) p_36347_).getHealth();
                    this.awardStat(Stats.DAMAGE_DEALT, Math.round(f5 * 10.0F));
                    if (j > 0) p_36347_.setSecondsOnFire(j * 4);
                    if (this.level() instanceof ServerLevel && f5 > 2.0F) {
                        int k = (int) ((double) f5 * 0.5D);
                        ((ServerLevel) this.level()).sendParticles(ParticleTypes.DAMAGE_INDICATOR, p_36347_.getX(), p_36347_.getY(0.5D), p_36347_.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                    }
                }
                this.causeFoodExhaustion(0.1F);
            } else {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.getSoundSource(), 1.0F, 1.0F);
                if (flag4) p_36347_.clearFire();
            }
        }
        this.resetAttackStrengthTicker();
    }
}
