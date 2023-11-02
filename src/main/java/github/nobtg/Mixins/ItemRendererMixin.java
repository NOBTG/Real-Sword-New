package github.nobtg.Mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import github.nobtg.RealSwordMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ItemRenderer.class, priority = 0x7fffffff)
public abstract class ItemRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract void renderQuadList(PoseStack p_115163_, VertexConsumer p_115164_, List<BakedQuad> p_115165_, ItemStack p_115166_, int p_115167_, int p_115168_);

    @Unique
    public ItemDisplayContext realSword$context;

    @Inject(method = "render", at = @At("HEAD"))
    private void render(ItemStack p_115144_, ItemDisplayContext p_270188_, boolean p_115146_, PoseStack p_115147_, MultiBufferSource p_115148_, int p_115149_, int p_115150_, BakedModel p_115151_, CallbackInfo ci) {
        realSword$context = p_270188_;
    }

    @Inject(method = "renderModelLists", at = @At("HEAD"))
    private void renderModelLists(BakedModel p_115190_, @NotNull ItemStack p_115191_, int p_115192_, int p_115193_, PoseStack p_115194_, VertexConsumer p_115195_, CallbackInfo ci) {
        if (realSword$context == ItemDisplayContext.GUI && p_115191_.getItem() == RealSwordMod.REAL_SWORD.get()) {
            RandomSource randomsource = RandomSource.create();
            long i = 42L;
            for (Direction direction : Direction.values()) {
                randomsource.setSeed(i);
                // Start Modify
                this.renderQuadList(p_115194_, this.minecraft.renderBuffers().bufferSource().getBuffer(RenderType.endPortal()), p_115190_.getQuads(null, direction, randomsource), p_115191_, p_115192_, p_115193_);
                // End Modify
            }
            randomsource.setSeed(i);
            // Start Modify
            this.renderQuadList(p_115194_, this.minecraft.renderBuffers().bufferSource().getBuffer(RenderType.endPortal()), p_115190_.getQuads(null, null, randomsource), p_115191_, p_115192_, p_115193_);
            // End Modify
        }
    }
}
