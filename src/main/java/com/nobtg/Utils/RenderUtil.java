package com.nobtg.Utils;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber
public abstract class RenderUtil {
    public static void ClientAttack(Minecraft minecraft) {
        Util.setAttack(true, minecraft);
    }

    @SubscribeEvent
    public static void RenderGuiEvent(RenderGuiEvent event) {
        if (!Util.isAttack(Minecraft.getInstance())) return;
        Minecraft mc = Minecraft.getInstance();
        event.getGuiGraphics().pose().pushPose();
        event.getGuiGraphics().pose().scale(2.0F, 2.0F, 2.0F);
        drawString(event.getGuiGraphics(), mc.getWindow().getGuiScaledWidth() / 2 / 2, GetterAndSetters.getColor1().getRGB());
        event.getGuiGraphics().pose().popPose();
        for (int i = 0; i < GetterAndSetters.getCount(); i++) {
            float rotationAngle = (float) (i / Math.random());
            RenderRainbowTexture(event.getGuiGraphics(), mc, rotationAngle);
        }
        GetterAndSetters.setCount((GetterAndSetters.getCount() % 16.0F) + 0.05F);
    }

    public static void RenderRainbowTexture(@NotNull GuiGraphics graphics, @NotNull Minecraft mc, float rotationAngle) {
        graphics.pose().pushPose();
        graphics.pose().mulPose(chooseDirection().rotationDegrees(rotationAngle));
        graphics.fillGradient(0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), GetterAndSetters.getColor1().getRGB(), GetterAndSetters.getColor2().getRGB());
        graphics.pose().popPose();
    }

    public static Axis chooseDirection() {
        int randomIndex = ThreadLocalRandom.current().nextInt(6) + 1;
        return switch (randomIndex) {
            case 1 -> Axis.XP;
            case 2 -> Axis.YN;
            case 3 -> Axis.YP;
            case 4 -> Axis.ZN;
            case 5 -> Axis.ZP;
            default -> Axis.XN;
        };
    }

    public static void drawString(@NotNull GuiGraphics guiGraphics, int p_281586_, int p_281743_) {
        Util.getFont().SetGuiOnly(true).drawInBatch(Component.translatable("deathScreen.title"), (float) p_281586_ - (float) Util.getFont().width(Component.translatable("deathScreen.title").getVisualOrderText()) / 2, 30.0F, p_281743_, true, guiGraphics.pose().last().pose(), guiGraphics.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
        flushIfUnmanaged(guiGraphics);
    }

    private static void flushIfUnmanaged(GuiGraphics guiGraphics) {
        if (!(boolean) Util.getField(guiGraphics, GuiGraphics.class, "managed", "f_285610_"))
            guiGraphics.flush();
    }
}
