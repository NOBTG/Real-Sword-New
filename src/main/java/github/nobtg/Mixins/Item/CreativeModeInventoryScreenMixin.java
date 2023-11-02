package github.nobtg.Mixins.Item;

import github.nobtg.RealSwordMod;
import github.nobtg.Utils.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = CreativeModeInventoryScreen.class, priority = 0x7fffffff)
public abstract class CreativeModeInventoryScreenMixin {
    @Inject(method = "getTooltipFromContainerItem", at = @At("RETURN"), cancellable = true)
    private void getTooltipFromContainerItem(ItemStack p_281769_, CallbackInfoReturnable<List<Component>> cir) {
        Screen this_ = (Screen) (Object) this;
        if (p_281769_.getItem() == RealSwordMod.REAL_SWORD.get() && Util.getField(this_, Screen.class, "minecraft", "f_96541_") != null) cir.setReturnValue(p_281769_.getTooltipLines(((Minecraft) Util.getField(this_, Screen.class, "minecraft", "f_96541_")).player, TooltipFlag.NORMAL));
    }
}
