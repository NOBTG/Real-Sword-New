package com.nobtg.Mixins;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Minecraft.class, priority = 0x7fffffff)
public abstract class MinecraftMixin {
    @Unique private boolean realSword$Attack = false;
}
