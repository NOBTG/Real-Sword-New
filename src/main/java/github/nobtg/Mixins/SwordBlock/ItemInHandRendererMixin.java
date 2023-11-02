package github.nobtg.Mixins.SwordBlock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import github.nobtg.Utils.Util;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemInHandRenderer.class, priority = 0x7fffffff)
public abstract class ItemInHandRendererMixin {
    @Shadow
    protected abstract void applyItemArmTransform(PoseStack matrices, HumanoidArm arm, float equippedProgress);

    @Inject(method = "renderArmWithItem",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"))
    private void renderSwordBlock(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, @NotNull ItemStack stack, float equippedProgress, PoseStack matrices, MultiBufferSource buffer, int combinedLight, CallbackInfo info) {
        if (stack.getUseAnimation() == Util.getUseAnim()) {
            boolean flag = (hand == InteractionHand.MAIN_HAND);
            HumanoidArm arm = flag ? player.getMainArm() : player.getMainArm().getOpposite();
            this.applyItemArmTransform(matrices, arm, equippedProgress);
            int horizontal = arm == HumanoidArm.RIGHT ? 1 : -1;
            matrices.translate(horizontal * -0.14142136F, 0.08F, 0.14142136F);
            matrices.mulPose(Axis.XP.rotationDegrees(-102.25F));
            matrices.mulPose(Axis.YP.rotationDegrees(horizontal * 13.365F));
            matrices.mulPose(Axis.ZP.rotationDegrees(horizontal * 78.05F));
        }
    }
}