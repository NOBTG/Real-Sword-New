package github.nobtg.Mixins.Item;

import github.nobtg.RealSwordMod;
import github.nobtg.Utils.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ItemStack.class, priority = 0x7fffffff)
public abstract class ItemStackMixin {
    @Shadow public abstract Item getItem();

    @Inject(method = "getTooltipLines", at = @At("HEAD"))
    private void getTooltipLines(Player p_41652_, TooltipFlag p_41653_, CallbackInfoReturnable<List<Component>> cir) {
        if (this.getItem() == RealSwordMod.REAL_SWORD.get()) p_41653_ = TooltipFlag.NORMAL;
    }

    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void addList(Player p_41652_, TooltipFlag p_41653_, @NotNull CallbackInfoReturnable<List<Component>> cir) {
        List<Component> list = cir.getReturnValue();
        if (this.getItem() != RealSwordMod.REAL_SWORD.get()) return;
        list.add(Component.literal(""));
        list.add(Component.literal(Util.getTooltip(Util.getTooltips().getFirst())));
        cir.setReturnValue(list);
    }
}
