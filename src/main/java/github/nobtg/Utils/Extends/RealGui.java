package github.nobtg.Utils.Extends;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import github.nobtg.Utils.ClientUtil;
import github.nobtg.Utils.EntityUtil;
import github.nobtg.Utils.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class RealGui extends ForgeGui {
    public RealGui(Minecraft p_232355_) {
        super(p_232355_);
    }

    public void renderSelectedItemName(@NotNull GuiGraphics p_283501_, int yShift) {
        if (!ClientUtil.getNoDrawScreenVal(this.minecraft)) super.renderSelectedItemName(p_283501_, yShift);
    }

    public void render(GuiGraphics guiGraphics, float partialTick) {
        super.render(guiGraphics, partialTick);
        if (ClientUtil.getNoDrawScreenVal(this.minecraft)) {
            Component component = Component.literal("NO DRAW SCREEN");
            RealFont font = Util.getFont().SetGuiOnly(true);
            int i = font.width(component);
            int j = (this.screenWidth - i) / 2;
            int k = this.screenHeight - Math.max(0, 59);
            if (this.minecraft.gameMode != null && !this.minecraft.gameMode.canHurtPlayer())
                k += 14;
            guiGraphics.fill(j - 2, k - 2, j + i + 2, k + 9 + 2, this.minecraft.options.getBackgroundColor(0));
            j = (this.screenWidth - i) / 2;
            guiGraphics.drawString(font, component, j, k, 0);
        }
    }

    protected void renderHearts(@NotNull GuiGraphics graphics, @NotNull Player player, int screen_width, int screen_height, int p_168693_, int p_168694_, float p_168695_, int p_168696_, int p_168697_, int p_168698_, boolean flag) {
        if (EntityUtil.getRealEntities().contains(player) && !Util.getAutoCombat(player)) {
            for (int i = 10 - 1; i >= 0; --i) {
                int a = (i / 10);
                float x = screen_width + (i % 10) * 8;
                float y = screen_height - a * 11;
                y -= 1;
                y += (float) Math.sin((i + 1) + (float) net.minecraft.Util.getMillis() / 300.0F);
                renderHeart(graphics, false, x, y, flag);
                if (flag && (i * 2) < 20) renderHeart(graphics, true, x, y, true);
                if ((i * 2) < 20) renderHeart(graphics, true, x, y, false);
            }
        } else
            super.renderHearts(graphics, player, screen_width, screen_height, p_168693_, p_168694_, p_168695_, p_168696_, p_168697_, p_168698_, flag);
    }

    public void renderHeart(GuiGraphics p_283024_, boolean isNormalHeart, float v, float v1, boolean flag) {
        int i;
        int index = flag ? 2 : 0;
        if (!isNormalHeart) i = flag ? 1 : 0;
        else i = index;
        int x = 16 + (index * 2 + i) * 9;
        this.innerBlit(p_283024_, v, v + 9, v1, v1 + 9, (float) x / 256, (float) (x + 9) / 256);
    }

    public void innerBlit(@NotNull GuiGraphics guiGraphics, float p_281399_, float p_283222_, float p_283615_, float p_283430_, float p_283247_, float p_282598_) {
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, p_281399_, p_283615_, 0).uv(p_283247_, 0).endVertex();
        bufferbuilder.vertex(matrix4f, p_281399_, p_283430_, 0).uv(p_283247_, 9.0F / 256).endVertex();
        bufferbuilder.vertex(matrix4f, p_283222_, p_283430_, 0).uv(p_282598_, 9.0F / 256).endVertex();
        bufferbuilder.vertex(matrix4f, p_283222_, p_283615_, 0).uv(p_282598_, 0).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }
}