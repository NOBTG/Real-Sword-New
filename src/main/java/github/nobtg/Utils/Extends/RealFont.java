package github.nobtg.Utils.Extends;

import github.nobtg.Utils.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.function.Function;

public class RealFont extends Font {
    private boolean GuiOnly = false;

    public RealFont(Function<ResourceLocation, FontSet> p_243253_, boolean p_243245_) {
        super(p_243253_, p_243245_);
    }

    public int drawInBatch(@NotNull FormattedCharSequence formattedCharSequence, float x, float y, int rgb, boolean b1, @NotNull Matrix4f matrix4f, @NotNull MultiBufferSource multiBufferSource, @NotNull DisplayMode mode, int i, int i1) {
        StringBuilder stringBuilder = new StringBuilder();
        formattedCharSequence.accept((index, style, codePoint) -> {
            stringBuilder.appendCodePoint(codePoint);
            return true;
        });
        String text = ChatFormatting.stripFormatting(stringBuilder.toString());
        float hue = (float) net.minecraft.Util.getMillis() / 700.0F % 1.0F;
        float hueStep = (float) (0.025 + ((Math.sin((float) net.minecraft.Util.getMillis() / 1200.0F) % 6.28318D) + 0.9) * 0.1475 / 3.6);
        if (text != null)
            for (int index = 0; index < text.length(); index++) {
                String s = String.valueOf(text.charAt(index));
                if (!GuiOnly)
                    super.drawInBatch(s, x, y, Util.getColor(index), b1, matrix4f, multiBufferSource, mode, i, i1);
                else {
                    int c = rgb & 0xFF000000 | Mth.hsvToRgb(hue, 0.8F, 1.0F);
                    super.drawInBatch(s, x, y, c, b1, matrix4f, multiBufferSource, mode, i, i1);
                    hue += hueStep;
                    hue %= 1.0F;
                }
                if (!GuiOnly) {
                    float of = y + ((float) Math.sin(((index + 1) + (float) net.minecraft.Util.getMillis() / 300.0F)));
                    for (float offset : new float[]{1F, 0.75F, 0.5F, 0.25F})
                        super.drawInBatch(s, x, of + offset, Util.getColor(index), b1, matrix4f, multiBufferSource, mode, i, i1);
                }
                x += this.width(s);
            }
        return (int) x;
    }

    public int drawInBatch(@NotNull String string, float x, float y, int rgb, boolean b, @NotNull Matrix4f matrix4f, @NotNull MultiBufferSource source, @NotNull DisplayMode mode, int i, int i1) {
        return this.drawInBatch(net.minecraft.network.chat.Component.literal(string).getVisualOrderText(), x, y, rgb, b, matrix4f, source, mode, i, i1);
    }

    public int drawInBatch(@NotNull Component component, float x, float y, int rgb, boolean b, @NotNull Matrix4f matrix4f, @NotNull MultiBufferSource source, @NotNull DisplayMode mode, int i, int i1) {
        return this.drawInBatch(component.getVisualOrderText(), x, y, rgb, b, matrix4f, source, mode, i, i1);
    }

    public RealFont SetGuiOnly(boolean b) {
        this.GuiOnly = b;
        return this;
    }
}