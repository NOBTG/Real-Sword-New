package github.nobtg.Mixins;

import github.nobtg.Utils.Other.Inject;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Minecraft.class, priority = 0x7fffffff)
public abstract class MinecraftMixin {
    @Unique @Inject
    private boolean realSword$Attack = false;
}
